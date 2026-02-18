package com.autogarage.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autogarage.domain.usecase.customer.CreateCustomerUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

@HiltViewModel
class CreateCustomerViewModel @Inject constructor(
    private val createCustomerUseCase: CreateCustomerUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateCustomerUiState())
    val uiState: StateFlow<CreateCustomerUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<CreateCustomerUiEvent>()
    val uiEvent: SharedFlow<CreateCustomerUiEvent> = _uiEvent.asSharedFlow()

    fun onNameChange(name: String) {
        _uiState.update { it.copy(name = name, nameError = null) }
    }

    fun onPhoneChange(phone: String) {
        // Only allow digits
        val filtered = phone.filter { it.isDigit() }
        if (filtered.length <= 10) {
            _uiState.update { it.copy(phone = filtered, phoneError = null) }
        }
    }

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, emailError = null) }
    }

    fun onAddressChange(address: String) {
        _uiState.update { it.copy(address = address) }
    }

    fun onGstNumberChange(gstNumber: String) {
        _uiState.update { it.copy(gstNumber = gstNumber) }
    }

    fun onNotesChange(notes: String) {
        _uiState.update { it.copy(notes = notes) }
    }

    fun onSaveClick() {
        if (!validateForm()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // ✅ INSTANT FEEDBACK: Navigate immediately, save in background
            _uiEvent.emit(CreateCustomerUiEvent.NavigateBackInstantly)


            // ✅ OPTIMIZATION: Launch in IO dispatcher for faster execution
            launch(Dispatchers.IO) {
                try {
                    // ✅ OPTIMIZATION: Add timeout to prevent hanging
                    withTimeout(10000L) { // 10 second timeout
                        val result = createCustomerUseCase(
                            CreateCustomerUseCase.Params(
                                name = _uiState.value.name.trim(),
                                phone = _uiState.value.phone.trim(),
                                email = _uiState.value.email.trim().takeIf { it.isNotBlank() },
                                address = _uiState.value.address.trim().takeIf { it.isNotBlank() },
                                gstNumber = _uiState.value.gstNumber.trim()
                                    .takeIf { it.isNotBlank() },
                                notes = _uiState.value.notes.trim().takeIf { it.isNotBlank() }
                            )
                        )

                        // Switch back to Main dispatcher for UI updates
                        withContext(Dispatchers.Main) {
                            result.fold(
                                onSuccess = { customerId ->
                                    _uiEvent.emit(CreateCustomerUiEvent.CustomerCreated(customerId))
                                },
                                onFailure = { error ->
                                    _uiState.update { it.copy(isLoading = false) }
                                    _uiEvent.emit(
                                        CreateCustomerUiEvent.ShowError(
                                            error.message ?: "Failed to create customer"
                                        )
                                    )
                                }
                            )
                        }
                    }
                }
                catch (e: TimeoutCancellationException) {
                    _uiState.update { it.copy(isLoading = false) }
                    _uiEvent.emit(CreateCustomerUiEvent.ShowError("Operation timed out. Please try again."))
                }
            }

            val result = createCustomerUseCase(
                CreateCustomerUseCase.Params(
                    name = _uiState.value.name.trim(),
                    phone = _uiState.value.phone.trim(),
                    email = _uiState.value.email.trim().takeIf { it.isNotBlank() },
                    address = _uiState.value.address.trim().takeIf { it.isNotBlank() },
                    gstNumber = _uiState.value.gstNumber.trim().takeIf { it.isNotBlank() },
                    notes = _uiState.value.notes.trim().takeIf { it.isNotBlank() }
                )
            )

            result.fold(
                onSuccess = { customerId ->
                    _uiEvent.emit(CreateCustomerUiEvent.CustomerCreated(customerId))
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false) }
                    _uiEvent.emit(
                        CreateCustomerUiEvent.ShowError(
                            error.message ?: "Failed to create customer"
                        )
                    )
                }
            )
        }
    }

    private fun validateForm(): Boolean {
        val state = _uiState.value
        var isValid = true

        if (state.name.isBlank()) {
            _uiState.update { it.copy(nameError = "Name is required") }
            isValid = false
        }

        if (state.phone.isBlank()) {
            _uiState.update { it.copy(phoneError = "Phone number is required") }
            isValid = false
        } else if (state.phone.length != 10) {
            _uiState.update { it.copy(phoneError = "Phone number must be 10 digits") }
            isValid = false
        }

        if (state.email.isNotBlank() && !isValidEmail(state.email)) {
            _uiState.update { it.copy(emailError = "Invalid email address") }
            isValid = false
        }

        return isValid
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}

data class CreateCustomerUiState(
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val gstNumber: String = "",
    val notes: String = "",
    val nameError: String? = null,
    val phoneError: String? = null,
    val emailError: String? = null,
    val isLoading: Boolean = false
)

sealed class CreateCustomerUiEvent {
    data class CustomerCreated(val customerId: Long) : CreateCustomerUiEvent()
    data class ShowError(val message: String) : CreateCustomerUiEvent()
    data object NavigateBackInstantly : CreateCustomerUiEvent()
}
