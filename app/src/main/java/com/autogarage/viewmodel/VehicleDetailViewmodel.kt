package com.autogarage.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autogarage.domain.model.JobCard
import com.autogarage.domain.model.Vehicle
import com.autogarage.domain.usecase.vehicle.DeleteVehicleUseCase
import com.autogarage.domain.usecase.vehicle.GetJobCardsByVehicleUseCase
import com.autogarage.domain.usecase.vehicle.GetVehicleByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VehicleDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getVehicleByIdUseCase: GetVehicleByIdUseCase,
    private val getJobCardsByVehicleUseCase: GetJobCardsByVehicleUseCase,
    private val deleteVehicleUseCase: DeleteVehicleUseCase
) : ViewModel() {

    private val vehicleId: Long = savedStateHandle.get<Long>("vehicleId") ?: 0L

    private val _uiState = MutableStateFlow(VehicleDetailUiState())
    val uiState: StateFlow<VehicleDetailUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<VehicleDetailUiEvent>()
    val uiEvent: SharedFlow<VehicleDetailUiEvent> = _uiEvent.asSharedFlow()

    init {
        loadVehicleDetails()
        loadJobCards()
    }

    private fun loadVehicleDetails() {
        viewModelScope.launch {
            getVehicleByIdUseCase(vehicleId).collect { vehicle ->
                if (vehicle != null) {
                    _uiState.update {
                        it.copy(
                            vehicle = vehicle,
                            isLoading = false,
                            error = null
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Vehicle not found"
                        )
                    }
                }
            }
        }
    }

    private fun loadJobCards() {
        viewModelScope.launch {
            getJobCardsByVehicleUseCase(vehicleId).collect { jobCards ->
                _uiState.update {
                    it.copy(
                        jobCards = jobCards,
                        isLoadingJobCards = false
                    )
                }
            }
        }
    }

    fun onEditClick() {
        viewModelScope.launch {
            _uiEvent.emit(VehicleDetailUiEvent.NavigateToEdit(vehicleId))
        }
    }

    fun onDeleteClick() {
        _uiState.update { it.copy(showDeleteDialog = true) }
    }

    fun onDeleteConfirm() {
        viewModelScope.launch {
            _uiState.update { it.copy(showDeleteDialog = false, isDeleting = true) }

            val vehicle = _uiState.value.vehicle
            if (vehicle != null) {
                deleteVehicleUseCase(vehicle).fold(
                    onSuccess = {
                        _uiEvent.emit(VehicleDetailUiEvent.VehicleDeleted)
                    },
                    onFailure = { error ->
                        _uiState.update { it.copy(isDeleting = false) }
                        _uiEvent.emit(
                            VehicleDetailUiEvent.ShowError(
                                error.message ?: "Failed to delete vehicle"
                            )
                        )
                    }
                )
            }
        }
    }

    fun onDeleteCancel() {
        _uiState.update { it.copy(showDeleteDialog = false) }
    }

    fun onJobCardClick(jobCardId: Long) {
        viewModelScope.launch {
            _uiEvent.emit(VehicleDetailUiEvent.NavigateToJobCard(jobCardId))
        }
    }

    fun onAddJobCardClick() {
        viewModelScope.launch {
            _uiEvent.emit(VehicleDetailUiEvent.NavigateToCreateJobCard(vehicleId))
        }
    }

    fun onCustomerClick() {
        viewModelScope.launch {
            _uiState.value.vehicle?.let { vehicle ->
                _uiEvent.emit(VehicleDetailUiEvent.NavigateToCustomer(vehicle.customerId))
            }
        }
    }

    fun onRefresh() {
        _uiState.update { it.copy(isLoading = true, isLoadingJobCards = true) }
        loadVehicleDetails()
        loadJobCards()
    }
}

data class VehicleDetailUiState(
    val vehicle: Vehicle? = null,
    val jobCards: List<JobCard> = emptyList(),
    val isLoading: Boolean = true,
    val isLoadingJobCards: Boolean = true,
    val isDeleting: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val error: String? = null
)

sealed class VehicleDetailUiEvent {
    data class NavigateToEdit(val vehicleId: Long) : VehicleDetailUiEvent()
    data class NavigateToJobCard(val jobCardId: Long) : VehicleDetailUiEvent()
    data class NavigateToCreateJobCard(val vehicleId: Long) : VehicleDetailUiEvent()
    data class NavigateToCustomer(val customerId: Long) : VehicleDetailUiEvent()
    data object VehicleDeleted : VehicleDetailUiEvent()
    data class ShowError(val message: String) : VehicleDetailUiEvent()
}