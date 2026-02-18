package com.autogarage.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autogarage.domain.model.Customer
import com.autogarage.domain.model.Priority
import com.autogarage.domain.model.Vehicle
import com.autogarage.domain.model.Worker
import com.autogarage.domain.usecase.customer.GetAllCustomersUseCase
import com.autogarage.domain.usecase.jobcard.CreateJobCardUseCase
import com.autogarage.domain.usecase.vehicle.GetVehiclesByCustomerUseCase
import com.autogarage.domain.usecase.worker.GetAvailableTechniciansUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateJobCardViewModel @Inject constructor(
    private val getAllCustomersUseCase: GetAllCustomersUseCase,
    private val getVehiclesByCustomerUseCase: GetVehiclesByCustomerUseCase,
    private val getAvailableTechniciansUseCase: GetAvailableTechniciansUseCase,
    private val createJobCardUseCase: CreateJobCardUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateJobCardUiState())
    val uiState: StateFlow<CreateJobCardUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<CreateJobCardUiEvent>()
    val uiEvent: SharedFlow<CreateJobCardUiEvent> = _uiEvent.asSharedFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                // ✅ FIX: Load customers and technicians properly
                launch {
                    getAllCustomersUseCase(Unit).collect { customers ->
                        _uiState.update { it.copy(customers = customers) }
                    }
                }

                launch {
                    // ✅ FIX: Get only ACTIVE technicians
                    getAvailableTechniciansUseCase(Unit)
                        .collect { technicians ->
                            _uiState.update {
                                it.copy(
                                    technicians = technicians,
                                    isLoadingData = false
                                )
                            }
                        }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoadingData = false
                        //error = e.message
                    )
                }
            }
        }
    }

    fun onCustomerSelected(customer: Customer) {
        _uiState.update {
            it.copy(
                selectedCustomer = customer,
                selectedVehicle = null,
                customerError = null
            )
        }

        // Load customer's vehicles
        viewModelScope.launch {
            getVehiclesByCustomerUseCase(customer.id).first().let { vehicles ->
                _uiState.update { it.copy(customerVehicles = vehicles) }
            }
        }
    }

    fun onVehicleSelected(vehicle: Vehicle) {
        _uiState.update {
            it.copy(
                selectedVehicle = vehicle,
                vehicleError = null,
                currentKilometers = vehicle.currentKilometers.toString()
            )
        }
    }

    fun onTechnicianSelected(technician: Worker?) {
        _uiState.update { it.copy(selectedTechnician = technician) }
    }

    fun onPrioritySelected(priority: Priority) {
        _uiState.update { it.copy(priority = priority) }
    }

    fun onCurrentKilometersChange(km: String) {
        val filtered = km.filter { it.isDigit() }
        _uiState.update { it.copy(currentKilometers = filtered, kilometersError = null) }
    }

    fun onComplaintsChange(complaints: String) {
        _uiState.update { it.copy(customerComplaints = complaints, complaintsError = null) }
    }

    fun onObservationsChange(observations: String) {
        _uiState.update { it.copy(mechanicObservations = observations) }
    }

    fun onEstimatedCompletionDateChange(date: String) {
        _uiState.update { it.copy(estimatedCompletionDate = date) }
    }

    fun onCreateJobCard() {
        if (!validateForm()) return

        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isCreating = true) }

            val result = createJobCardUseCase(
                CreateJobCardUseCase.Params(
                    vehicle = state.selectedVehicle!!,
                    currentKilometers = state.currentKilometers.toInt(),
                    customerComplaints = state.customerComplaints,
                    assignedTechnician = state.selectedTechnician,
                    priority = state.priority,
                    estimatedCompletionDate = state.estimatedCompletionDate.takeIf { it.isNotBlank() }
                )
            )

            result.fold(
                onSuccess = { jobCardId ->
                    _uiEvent.emit(CreateJobCardUiEvent.JobCardCreated(jobCardId))
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isCreating = false) }
                    _uiEvent.emit(
                        CreateJobCardUiEvent.ShowError(
                            error.message ?: "Failed to create job card"
                        )
                    )
                }
            )
        }
    }

    private fun validateForm(): Boolean {
        val state = _uiState.value
        var isValid = true

        if (state.selectedCustomer == null) {
            _uiState.update { it.copy(customerError = "Please select a customer") }
            isValid = false
        }

        if (state.selectedVehicle == null) {
            _uiState.update { it.copy(vehicleError = "Please select a vehicle") }
            isValid = false
        }

        if (state.currentKilometers.isBlank()) {
            _uiState.update { it.copy(kilometersError = "Current kilometers is required") }
            isValid = false
        }

        if (state.customerComplaints.isBlank()) {
            _uiState.update { it.copy(complaintsError = "Customer complaints are required") }
            isValid = false
        }

        return isValid
    }
}

data class CreateJobCardUiState(
    val customers: List<Customer> = emptyList(),
    val customerVehicles: List<Vehicle> = emptyList(),
    val technicians: List<Worker> = emptyList(),
    val selectedCustomer: Customer? = null,
    val selectedVehicle: Vehicle? = null,
    val selectedTechnician: Worker? = null,
    val priority: Priority = Priority.NORMAL,
    val currentKilometers: String = "",
    val customerComplaints: String = "",
    val mechanicObservations: String = "",
    val estimatedCompletionDate: String = "",
    val customerError: String? = null,
    val vehicleError: String? = null,
    val kilometersError: String? = null,
    val complaintsError: String? = null,
    val isLoadingData: Boolean = true,
    val isCreating: Boolean = false
)

sealed class CreateJobCardUiEvent {
    data class JobCardCreated(val jobCardId: Long) : CreateJobCardUiEvent()
    data class ShowError(val message: String) : CreateJobCardUiEvent()
}
