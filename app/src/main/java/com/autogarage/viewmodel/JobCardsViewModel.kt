// ===========================================================================
// FIXED JobCardsViewModel.kt
// ===========================================================================
package com.autogarage.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autogarage.domain.model.JobCard
import com.autogarage.domain.model.JobCardStatus
import com.autogarage.domain.usecase.jobcard.GetAllJobCardsUseCase
import com.autogarage.domain.usecase.jobcard.GetJobCardsByStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class JobCardsViewModel @Inject constructor(
    private val getAllJobCardsUseCase: GetAllJobCardsUseCase,
    private val getJobCardsByStatusUseCase: GetJobCardsByStatusUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(JobCardsUiState())
    val uiState: StateFlow<JobCardsUiState> = _uiState.asStateFlow()

    private val _selectedStatus = MutableStateFlow<JobCardStatus?>(null)
    val selectedStatus: StateFlow<JobCardStatus?> = _selectedStatus.asStateFlow()

    private val _uiEvent = MutableSharedFlow<JobCardUiEvent>()
    val uiEvent: SharedFlow<JobCardUiEvent> = _uiEvent.asSharedFlow()

    init {
        loadJobCards()
    }

    private fun loadJobCards() {
        viewModelScope.launch {
            // ✅ FIX: Properly handle the flow collection
            _selectedStatus
                .flatMapLatest { status ->
                    _uiState.update { it.copy(isLoading = true, error = null) }

                    if (status == null) {
                        getAllJobCardsUseCase(Unit)
                    } else {
                        getJobCardsByStatusUseCase(status)
                    }
                }
                .catch { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to load job cards"
                        )
                    }
                }
                .collect { jobCards ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            jobCards = jobCards,
                            error = null
                        )
                    }
                }
        }
    }

    fun onStatusFilterChange(status: JobCardStatus?) {
        _selectedStatus.value = status
    }

    fun onJobCardClick(jobCardId: Long) {
        viewModelScope.launch {
            _uiEvent.emit(JobCardUiEvent.NavigateToJobCardDetail(jobCardId))
        }
    }

    fun onCreateJobCardClick() {
        viewModelScope.launch {
            _uiEvent.emit(JobCardUiEvent.NavigateToCreateJobCard)
        }
    }

    fun onRefresh() {
        // Trigger reload by resetting the status
        val currentStatus = _selectedStatus.value
        _selectedStatus.value = null
        _selectedStatus.value = currentStatus
    }
}

data class JobCardsUiState(
    val isLoading: Boolean = true,
    val jobCards: List<JobCard> = emptyList(),
    val error: String? = null
)

sealed class JobCardUiEvent {
    data class NavigateToJobCardDetail(val jobCardId: Long) : JobCardUiEvent()
    data object NavigateToCreateJobCard : JobCardUiEvent()
    data class ShowMessage(val message: String) : JobCardUiEvent()
}


//package com.autogarage.viewmodel
//
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.autogarage.domain.model.JobCard
//import com.autogarage.domain.model.JobCardStatus
//import com.autogarage.domain.repository.CustomerRepository
//import com.autogarage.domain.repository.InventoryRepository
//import com.autogarage.domain.repository.VehicleRepository
//import com.autogarage.domain.usecase.jobcard.GetAllJobCardsUseCase
//import com.autogarage.domain.usecase.jobcard.GetJobCardsByStatusUseCase
//import dagger.hilt.android.lifecycle.HiltViewModel
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.flow.*
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//import javax.inject.Inject
//import javax.inject.Singleton
//
//@HiltViewModel
//class JobCardsViewModel @Inject constructor(
//    private val getAllJobCardsUseCase: GetAllJobCardsUseCase,
//    private val getJobCardsByStatusUseCase: GetJobCardsByStatusUseCase,
//    private val prefetchService: PrefetchService
//) : ViewModel() {
//    private val _selectedFilter = MutableStateFlow<JobCardFilter>(JobCardFilter.All)
//    val selectedFilter: StateFlow<JobCardFilter> = _selectedFilter.asStateFlow()
//    private val _selectedStatus = MutableStateFlow<JobCardStatus?>(null)
//    val selectedStatus: StateFlow<JobCardStatus?> = _selectedStatus.asStateFlow()
//    private val _searchQuery: MutableStateFlow<String> = MutableStateFlow("")
//    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
//    private val _uiState = MutableStateFlow(JobCardsUiState())
//    val uiState: StateFlow<JobCardsUiState> = _uiState.asStateFlow()
//    private val _uiEvent = MutableSharedFlow<JobCardUiEvent>()
//    val uiEvent: SharedFlow<JobCardUiEvent> = _uiEvent.asSharedFlow()
//    init {
//        // ✅ OPTIMIZATION: Prefetch related data
//        prefetchRelatedData()
//        loadJobCards()
//    }
//    private fun prefetchRelatedData() {
//        viewModelScope.launch {
//            // Prefetch customers in background
//            prefetchService.prefetchCustomers()
//
//            // Prefetch vehicles
//            prefetchService.prefetchVehicles()
//
//            // Prefetch inventory
//            prefetchService.prefetchInventoryItems()
//        }
//    }
//
//    private fun loadJobCards() {
//        viewModelScope.launch {
//            // ✅ FIX: Properly handle the flow collection
//            _selectedStatus
//                .flatMapLatest { status ->
//                    _uiState.update { it.copy(isLoading = true, error = null) }
//
//                    if (status == null) {
//                        getAllJobCardsUseCase(Unit)
//                    } else {
//                        getJobCardsByStatusUseCase(status)
//                    }
//                }
//                .catch { error ->
//                    _uiState.update {
//                        it.copy(
//                            isLoading = false,
//                            error = error.message ?: "Failed to load job cards"
//                        )
//                    }
//                }
//                .collect { jobCards ->
//                    _uiState.update {
//                        it.copy(
//                            isLoading = false,
//                            jobCards = jobCards,
//                            error = null
//                        )
//                    }
//                }
//        }
//    }
//
//    fun onStatusFilterChange(status: JobCardStatus?) {
//        _selectedStatus.value = status
//    }
//
//    fun onSearchQueryChange(query: String) {
//        _searchQuery.value = query
//    }
//
//    fun onJobCardClick(jobCardId: Long) {
//        viewModelScope.launch {
//            _uiEvent.emit(JobCardUiEvent.NavigateToJobCardDetail(jobCardId))
//        }
//    }
//
//    fun onCreateJobCardClick() {
//        viewModelScope.launch {
//            _uiEvent.emit(JobCardUiEvent.NavigateToCreateJobCard)
//        }
//    }
//
//    fun onRefresh() {
//        loadJobCards()
//    }
//}
//
//enum class JobCardFilter(val displayName: String) {
//    All("All"),
//    Pending("Pending"),
//    InProgress("In Progress"),
//    Completed("Completed")
//}
//
//data class JobCardsUiState(
//    val isLoading: Boolean = true,
//    val jobCards: List<JobCard> = emptyList(),
//    val error: String? = null
//)
//
//sealed class JobCardUiEvent {
//    data class NavigateToJobCardDetail(val jobCardId: Long) : JobCardUiEvent()
//    data object NavigateToCreateJobCard : JobCardUiEvent()
//    data class ShowMessage(val message: String) : JobCardUiEvent()
//}
//
//@Singleton
//class PrefetchService @Inject constructor(
//    private val customerRepository: CustomerRepository,
//    private val vehicleRepository: VehicleRepository,
//    private val inventoryRepository: InventoryRepository
//) {
//
//    suspend fun prefetchCustomers() {
//        withContext(Dispatchers.IO) {
//            // Load first 50 customers into memory
//            customerRepository.getRecentCustomers(limit = 50).first()
//        }
//    }
//
//    suspend fun prefetchVehicles() {
//        withContext(Dispatchers.IO) {
//            vehicleRepository.getRecentVehicles(limit = 50).first()
//        }
//    }
//
//    suspend fun prefetchInventoryItems() {
//        withContext(Dispatchers.IO) {
//            inventoryRepository.getAllActiveItems().first()
//        }
//    }
//}
