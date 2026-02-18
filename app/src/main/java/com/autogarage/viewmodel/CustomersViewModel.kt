package com.autogarage.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autogarage.domain.model.Customer
import com.autogarage.domain.usecase.customer.DeleteCustomerUseCase
import com.autogarage.domain.usecase.customer.GetAllCustomersUseCase
import com.autogarage.domain.usecase.customer.SearchCustomersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomersViewModel @Inject constructor(
    private val getAllCustomersUseCase: GetAllCustomersUseCase,
    private val searchCustomersUseCase: SearchCustomersUseCase,
    private val deleteCustomerUseCase: DeleteCustomerUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _uiState = MutableStateFlow(CustomersUiState())
    val uiState: StateFlow<CustomersUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<CustomerUiEvent>()
    val uiEvent: SharedFlow<CustomerUiEvent> = _uiEvent.asSharedFlow()

    init {
        loadCustomers()
    }

    // ✅ OPTIMIZATION: Use stateIn to share flow between multiple collectors
    val customers: StateFlow<List<Customer>> = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.isBlank()) {
                getAllCustomersUseCase(Unit)
            } else {
                searchCustomersUseCase(query)
            }
        }
        .catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000), // Stop after 5s of no subscribers
            initialValue = emptyList()
        )

    @OptIn(FlowPreview::class)
    private fun loadCustomers() {
        viewModelScope.launch {
            try {
                _searchQuery
                    .debounce(300)
                    .flatMapLatest { query ->
                        if (query.isBlank()) {
                            getAllCustomersUseCase(Unit)
                        } else {
                            searchCustomersUseCase(query)
                        }
                    }
                    .catch { error ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = error.message ?: "Unknown error"
                            )
                        }
                    }
                    .collect { customers ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                customers = customers,
                                error = null
                            )
                        }
                    }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load customers"
                    )
                }
            }
        }
    }


    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onCustomerClick(customerId: Long) {
        viewModelScope.launch {
            _uiEvent.emit(CustomerUiEvent.NavigateToCustomerDetail(customerId))
        }
    }

    fun onAddCustomerClick() {
        viewModelScope.launch {
            _uiEvent.emit(CustomerUiEvent.NavigateToAddCustomer)
        }
    }

    // ✅ NEW: Delete customer functionality
    fun onDeleteCustomerClick(customer: Customer) {
        _uiState.update { it.copy(customerToDelete = customer, showDeleteDialog = true) }
    }

    fun onDeleteDialogDismiss() {
        _uiState.update { it.copy(customerToDelete = null, showDeleteDialog = false) }
    }

    fun onConfirmDelete() {
        val customer = _uiState.value.customerToDelete ?: return

        viewModelScope.launch {
            // Optimistic update
            _uiState.update {
                it.copy(
                    customers = it.customers.filter { c -> c.id != customer.id },
                    showDeleteDialog = false,
                    customerToDelete = null,
                    isDeleting = true
                )
            }

            deleteCustomerUseCase(customer).fold(
                onSuccess = {
                    _uiState.update { it.copy(isDeleting = false) }
                    _uiEvent.emit(CustomerUiEvent.ShowMessage("Customer deleted"))
                },
                onFailure = { error ->
                    // Rollback
                    _uiState.update {
                        it.copy(
                            customers = (it.customers + customer).sortedBy { c -> c.name },
                            isDeleting = false
                        )
                    }
                    _uiEvent.emit(
                        CustomerUiEvent.ShowError(
                            error.message ?: "Failed to delete customer"
                        )
                    )
                }
            )
        }
    }


    fun onRefresh() {
        _searchQuery.value = ""
        loadCustomers()
    }
}

data class CustomersUiState(
    val isLoading: Boolean = true,
    val customers: List<Customer> = emptyList(),
    val error: String? = null,
    val showDeleteDialog: Boolean = false,
    val customerToDelete: Customer? = null,
    val isDeleting: Boolean = false
)

sealed class CustomerUiEvent {
    data class NavigateToCustomerDetail(val customerId: Long) : CustomerUiEvent()
    data object NavigateToAddCustomer : CustomerUiEvent()
    data class ShowMessage(val message: String) : CustomerUiEvent()
    data class ShowError(val message: String) : CustomerUiEvent()

}
