package com.autogarage.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autogarage.domain.usecase.reports.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val getRevenueReportUseCase: GetRevenueReportUseCase,
    private val getJobCardStatsUseCase: GetJobCardStatsUseCase,
    private val getCustomerStatsUseCase: GetCustomerStatsUseCase,
    private val getInventoryStatsUseCase: GetInventoryStatsUseCase,
    private val getWorkerPerformanceUseCase: GetWorkerPerformanceUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<ReportsUiEvent>()
    val uiEvent: SharedFlow<ReportsUiEvent> = _uiEvent.asSharedFlow()

    private val _selectedDateRange = MutableStateFlow(DateRange.THIS_MONTH)
    val selectedDateRange: StateFlow<DateRange> = _selectedDateRange.asStateFlow()

    init {
        loadReports()
    }

    fun onDateRangeChange(dateRange: DateRange) {
        _selectedDateRange.value = dateRange
        loadReports()
    }

    fun onCustomDateRangeSelected(startDate: Long, endDate: Long) {
        _selectedDateRange.value = DateRange.CUSTOM
        _uiState.update {
            it.copy(
                customStartDate = startDate,
                customEndDate = endDate
            )
        }
        loadReports()
    }

    private fun loadReports() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val (startDate, endDate) = getDateRangeTimestamps()

                // ✅ Add debug logging
                Log.d("ReportsViewModel", "Loading reports for date range: $startDate to $endDate")

                // Load all reports in parallel
                launch { loadRevenueReport(startDate, endDate) }
                launch { loadJobCardStats(startDate, endDate) }
                launch { loadCustomerStats(startDate, endDate) }
                launch { loadInventoryStats(startDate, endDate) }
                launch { loadWorkerPerformance(startDate, endDate) }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load reports"
                    )
                }
            }
        }
    }

    private suspend fun loadRevenueReport(startDate: Long, endDate: Long) {
        val result = getRevenueReportUseCase(
            GetRevenueReportUseCase.Params(startDate, endDate)
        )

        result.fold(
            onSuccess = { report ->
                _uiState.update {
                    it.copy(
                        revenueReport = report,
                        isLoading = false
                    )
                }
            },
            onFailure = { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to load revenue report"
                    )
                }
            }
        )
    }

    private suspend fun loadJobCardStats(startDate: Long, endDate: Long) {
        val result = getJobCardStatsUseCase(
            GetJobCardStatsUseCase.Params(startDate, endDate)
        )

        result.fold(
            onSuccess = { stats ->
                _uiState.update { it.copy(jobCardStats = stats) }
            },
            onFailure = { /* Handle silently or show error */ }
        )
    }

    private suspend fun loadCustomerStats(startDate: Long, endDate: Long) {
        val result = getCustomerStatsUseCase(
            GetCustomerStatsUseCase.Params(startDate, endDate)
        )

        result.fold(
            onSuccess = { stats ->
                _uiState.update { it.copy(customerStats = stats) }
            },
            onFailure = { /* Handle silently */ }
        )
    }

    private suspend fun loadInventoryStats(startDate: Long, endDate: Long) {
        val result = getInventoryStatsUseCase(
            GetInventoryStatsUseCase.Params(startDate, endDate)
        )

        result.fold(
            onSuccess = { stats ->
                _uiState.update { it.copy(inventoryStats = stats) }
            },
            onFailure = { /* Handle silently */ }
        )
    }

    private suspend fun loadWorkerPerformance(startDate: Long, endDate: Long) {
        Log.d("ReportsViewModel", "Loading worker performance...")

        val result = getWorkerPerformanceUseCase(
            GetWorkerPerformanceUseCase.Params(startDate, endDate)
        )

        result.fold(
            onSuccess = { performance ->
                Log.d("ReportsViewModel", "Worker performance loaded: ${performance.workers.size} workers")
                performance.workers.forEach { worker ->
                    Log.d("ReportsViewModel", "Worker: ${worker.workerName}, Jobs: ${worker.totalJobsAssigned}, Revenue: ${worker.revenueGenerated}")
                }

                _uiState.update { it.copy(workerPerformance = performance) }
            },
            onFailure = { error ->
                Log.e("ReportsViewModel", "Failed to load worker performance", error)
                // ✅ Don't show error, just leave it empty
                _uiState.update {
                    it.copy(
                        workerPerformance = WorkerPerformance(emptyList())
                    )
                }
            }
        )
    }

    private fun getDateRangeTimestamps(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        val endDate = calendar.timeInMillis

        val startDate = when (_selectedDateRange.value) {
            DateRange.TODAY -> {
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                calendar.timeInMillis
            }
            DateRange.THIS_WEEK -> {
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                calendar.timeInMillis
            }
            DateRange.THIS_MONTH -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                calendar.timeInMillis
            }
            DateRange.THIS_QUARTER -> {
                val month = calendar.get(Calendar.MONTH)
                val quarterStartMonth = (month / 3) * 3
                calendar.set(Calendar.MONTH, quarterStartMonth)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                calendar.timeInMillis
            }
            DateRange.THIS_YEAR -> {
                calendar.set(Calendar.DAY_OF_YEAR, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                calendar.timeInMillis
            }
            DateRange.CUSTOM -> {
                _uiState.value.customStartDate ?: 0L
            }
        }

        val actualEndDate = if (_selectedDateRange.value == DateRange.CUSTOM) {
            _uiState.value.customEndDate ?: endDate
        } else {
            endDate
        }

        return Pair(startDate, actualEndDate)
    }

    fun onExportReport(type: ReportType) {
        viewModelScope.launch {
            _uiEvent.emit(ReportsUiEvent.ShowMessage("Export feature coming soon"))
        }
    }

    fun onRefresh() {
        loadReports()
    }
}

data class ReportsUiState(
    val isLoading: Boolean = true,
    val revenueReport: RevenueReport? = null,
    val jobCardStats: JobCardStats? = null,
    val customerStats: CustomerStats? = null,
    val inventoryStats: InventoryStats? = null,
    val workerPerformance: WorkerPerformance? = null,
    val customStartDate: Long? = null,
    val customEndDate: Long? = null,
    val error: String? = null
)

enum class DateRange {
    TODAY, THIS_WEEK, THIS_MONTH, THIS_QUARTER, THIS_YEAR, CUSTOM
}

enum class ReportType {
    REVENUE, JOB_CARDS, CUSTOMERS, INVENTORY, WORKERS, ALL
}

sealed class ReportsUiEvent {
    data class ShowMessage(val message: String) : ReportsUiEvent()
    data class ShowError(val message: String) : ReportsUiEvent()
}