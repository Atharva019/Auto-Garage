package com.autogarage.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autogarage.domain.model.Customer
import com.autogarage.domain.model.JobCard
import com.autogarage.domain.model.Vehicle
import com.autogarage.domain.usecase.customer.GetCustomerByIdUseCase
import com.autogarage.domain.usecase.vehicle.GetJobCardsByVehicleUseCase
import com.autogarage.domain.usecase.vehicle.GetVehiclesByCustomerUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomerDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getCustomerByIdUseCase: GetCustomerByIdUseCase,
    private val getVehiclesByCustomerUseCase: GetVehiclesByCustomerUseCase,
    private val getJobCardsByVehicleUseCase: GetJobCardsByVehicleUseCase
) : ViewModel() {

    private val customerId: Long = savedStateHandle.get<Long>("customerId") ?: 0L

    private val _uiState = MutableStateFlow(CustomerDetailUiState())
    val uiState: StateFlow<CustomerDetailUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<CustomerDetailUiEvent>()
    val uiEvent: SharedFlow<CustomerDetailUiEvent> = _uiEvent.asSharedFlow()

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    init {
        loadCustomerDetails()
    }

    private fun loadCustomerDetails() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                combine(
                    getCustomerByIdUseCase(customerId),
                    getVehiclesByCustomerUseCase(customerId)
                ) { customer, vehicles ->
                    Pair(customer, vehicles)
                }.collect { (customer, vehicles) ->
                    if (customer != null) {
                        // Load service history for all vehicles
                        val allJobCards = mutableListOf<JobCard>()
                        vehicles.forEach { vehicle ->
                            getJobCardsByVehicleUseCase(vehicle.id).first().let { jobCards ->
                                allJobCards.addAll(jobCards)
                            }
                        }

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                customer = customer,
                                vehicles = vehicles,
                                serviceHistory = allJobCards.sortedByDescending { it.createdAt },
                                error = null
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "Customer not found"
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load customer details"
                    )
                }
            }
        }
    }

    fun onTabSelected(index: Int) {
        _selectedTab.value = index
    }

    fun onAddVehicleClick() {
        viewModelScope.launch {
            _uiEvent.emit(CustomerDetailUiEvent.NavigateToAddVehicle(customerId))
        }
    }

    fun onVehicleClick(vehicleId: Long) {
        viewModelScope.launch {
            _uiEvent.emit(CustomerDetailUiEvent.NavigateToVehicleDetail(vehicleId))
        }
    }

    fun onJobCardClick(jobCardId: Long) {
        viewModelScope.launch {
            _uiEvent.emit(CustomerDetailUiEvent.NavigateToJobCardDetail(jobCardId))
        }
    }

    fun onEditCustomerClick() {
        viewModelScope.launch {
            _uiEvent.emit(CustomerDetailUiEvent.NavigateToEditCustomer(customerId))
        }
    }

    fun onCallClick() {
        val customer = _uiState.value.customer ?: return
        viewModelScope.launch {
            _uiEvent.emit(CustomerDetailUiEvent.MakePhoneCall(customer.phone))
        }
    }

    fun onEmailClick() {
        val customer = _uiState.value.customer ?: return
        customer.email?.let { email ->
            viewModelScope.launch {
                _uiEvent.emit(CustomerDetailUiEvent.SendEmail(email))
            }
        }
    }

    fun onRefresh() {
        loadCustomerDetails()
    }
}

data class CustomerDetailUiState(
    val isLoading: Boolean = true,
    val customer: Customer? = null,
    val vehicles: List<Vehicle> = emptyList(),
    val serviceHistory: List<JobCard> = emptyList(),
    val error: String? = null
)

sealed class CustomerDetailUiEvent {
    data class NavigateToAddVehicle(val customerId: Long) : CustomerDetailUiEvent()
    data class NavigateToVehicleDetail(val vehicleId: Long) : CustomerDetailUiEvent()
    data class NavigateToJobCardDetail(val jobCardId: Long) : CustomerDetailUiEvent()
    data class NavigateToEditCustomer(val customerId: Long) : CustomerDetailUiEvent()
    data class MakePhoneCall(val phoneNumber: String) : CustomerDetailUiEvent()
    data class SendEmail(val email: String) : CustomerDetailUiEvent()
    data class ShowMessage(val message: String) : CustomerDetailUiEvent()
}