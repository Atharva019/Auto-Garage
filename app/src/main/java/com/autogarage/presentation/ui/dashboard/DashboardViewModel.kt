package com.autogarage.presentation.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autogarage.domain.model.JobCard
import com.autogarage.domain.model.JobCardStatus
import com.autogarage.domain.usecase.customer.GetAllCustomersUseCase
import com.autogarage.domain.usecase.inventory.GetLowStockItemsUseCase
import com.autogarage.domain.usecase.jobcard.GetDashboardStatsUseCase
import com.autogarage.domain.usecase.jobcard.GetJobCardsByStatusUseCase
import com.autogarage.domain.usecase.reports.DashboardSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.autogarage.domain.usecase.reports.GetDashboardSummaryUseCase
import android.util.Log

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getDashboardStatsUseCase: GetDashboardStatsUseCase,
    private val getJobCardsByStatusUseCase: GetJobCardsByStatusUseCase,
    private val getAllCustomersUseCase: GetAllCustomersUseCase,
    private val getLowStockItemsUseCase: GetLowStockItemsUseCase,
    private val getDashboardSummaryUseCase: GetDashboardSummaryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<DashboardUiEvent>()
    val uiEvent: SharedFlow<DashboardUiEvent> = _uiEvent.asSharedFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // ✅ FIX: Load reports summary in parallel
                launch {
                    Log.d("DashboardViewModel", "Loading reports summary...")
                    val summaryResult = getDashboardSummaryUseCase(Unit)

                    summaryResult.fold(
                        onSuccess = { summary ->
                            Log.d("DashboardViewModel", "Reports summary loaded: " +
                                    "Today: ${summary.todayRevenue}, " +
                                    "Month: ${summary.monthRevenue}, " +
                                    "Growth: ${summary.revenueGrowth}%")

                            _uiState.update { it.copy(reportsSummary = summary) }
                        },
                        onFailure = { error ->
                            Log.e("DashboardViewModel", "Failed to load reports summary", error)
                            // Continue with basic dashboard even if reports fail
                        }
                    )
                }

                // ✅ Load dashboard stats
                combine(
                    getDashboardStatsUseCase(Unit),
                    getAllCustomersUseCase(Unit),
                    getJobCardsByStatusUseCase(JobCardStatus.PENDING),
                    getLowStockItemsUseCase(Unit)
                ) { stats, customers, pendingJobs, lowStockItems ->
                    DashboardData(
                        totalJobCards = stats.totalJobCards,
                        pendingJobCards = stats.pendingCount,
                        inProgressJobCards = stats.inProgressCount,
                        completedJobCards = stats.completedCount,
                        totalCustomers = customers.size,
                        lowStockCount = lowStockItems.size,
                        recentPendingJobs = pendingJobs.take(5)
                    )
                }.collect { data ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            dashboardData = data,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "Error loading dashboard", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Unknown error occurred"
                    )
                }
            }
        }
    }

    fun onRefresh() {
        loadDashboardData()
    }

    fun onMetricClick(metric: DashboardMetric) {
        viewModelScope.launch {
            _uiEvent.emit(DashboardUiEvent.NavigateToMetric(metric))
        }
    }

    fun onNavigateToReports() {
        viewModelScope.launch {
            _uiEvent.emit(DashboardUiEvent.NavigateToReports)
        }
    }
}

data class DashboardUiState(
    val isLoading: Boolean = false,
    val dashboardData: DashboardData? = null,
    val reportsSummary: DashboardSummary? = null, // ✅ Added
    val error: String? = null
)

data class DashboardData(
    val totalJobCards: Int = 0,
    val pendingJobCards: Int = 0,
    val inProgressJobCards: Int = 0,
    val completedJobCards: Int = 0,
    val totalCustomers: Int = 0,
    val lowStockCount: Int = 0,
    val recentPendingJobs: List<JobCard> = emptyList()
)

sealed class DashboardUiEvent {
    data class NavigateToMetric(val metric: DashboardMetric) : DashboardUiEvent()
    data object NavigateToCreateJobCard : DashboardUiEvent()
    data class ShowError(val message: String) : DashboardUiEvent()
    data object NavigateToReports : DashboardUiEvent()
}

enum class DashboardMetric {
    PENDING_JOBS, IN_PROGRESS_JOBS, COMPLETED_JOBS,
    TOTAL_CUSTOMERS, LOW_STOCK_ITEMS
}