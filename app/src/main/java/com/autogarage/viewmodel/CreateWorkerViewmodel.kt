// ===========================================================================
// CreateWorkerViewModel.kt - COMPLETE & FIXED
// ===========================================================================
package com.autogarage.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autogarage.domain.model.Worker
import com.autogarage.domain.model.WorkerRole
import com.autogarage.domain.model.WorkerStatus
import com.autogarage.domain.usecase.worker.CreateWorkerUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class CreateWorkerViewModel @Inject constructor(
    private val createWorkerUseCase: CreateWorkerUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateWorkerUiState())
    val uiState: StateFlow<CreateWorkerUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<CreateWorkerUiEvent>()
    val uiEvent: SharedFlow<CreateWorkerUiEvent> = _uiEvent.asSharedFlow()

    fun onNameChange(name: String) {
        _uiState.update { state -> state.copy(name = name, nameError = null) }
    }

    fun onPhoneChange(phone: String) {
        val filtered = phone.filter { it.isDigit() }
        if (filtered.length <= 10) {
            _uiState.update { state -> state.copy(phone = filtered, phoneError = null) }
        }
    }

    fun onEmailChange(email: String) {
        _uiState.update { state -> state.copy(email = email, emailError = null) }
    }

    fun onRoleChange(role: WorkerRole) {
        _uiState.update { state -> state.copy(role = role, roleError = null) }
    }

    fun onSpecializationChange(specialization: String) {
        _uiState.update { state -> state.copy(specialization = specialization) }
    }

    fun onDateOfJoiningChange(date: LocalDate) {
        _uiState.update { state -> state.copy(dateOfJoining = date, dateError = null) }
    }

    fun onSalaryChange(salary: String) {
        val filtered = salary.filter { it.isDigit() || it == '.' }
        _uiState.update { state -> state.copy(salary = filtered, salaryError = null) }
    }

    fun onAddressChange(address: String) {
        _uiState.update { state -> state.copy(address = address) }
    }

    fun onEmergencyContactChange(contact: String) {
        val filtered = contact.filter { it.isDigit() }
        if (filtered.length <= 10) {
            _uiState.update { state -> state.copy(emergencyContact = filtered) }
        }
    }

//    fun onAadharNumberChange(aadhar: String) {
//        val filtered = aadhar.filter { it.isDigit() }
//        if (filtered.length <= 12) {
//            _uiState.update { state -> state.copy(aadharNumber = filtered) }
//        }
//    }
//
//    fun onPanNumberChange(pan: String) {
//        val filtered = pan.uppercase().filter { it.isLetterOrDigit() }
//        if (filtered.length <= 10) {
//            _uiState.update { state -> state.copy(panNumber = filtered) }
//        }
//    }

    fun onSaveClick() {
        if (!validateForm()) return

        viewModelScope.launch {
            _uiState.update { state -> state.copy(isLoading = true) }

            // ✅ INSTANT FEEDBACK: Navigate immediately
            _uiEvent.emit(CreateWorkerUiEvent.NavigateBackInstantly)

            withContext(Dispatchers.IO) {
                val worker = Worker(
                    id = 0, // Auto-generated
                    name = _uiState.value.name.trim(),
                    phone = _uiState.value.phone.trim(),
                    email = _uiState.value.email.trim().takeIf { it.isNotBlank() },
                    role = _uiState.value.role!!,
                    specialization = _uiState.value.specialization.trim().takeIf { it.isNotBlank() },
                    dateOfJoining = _uiState.value.dateOfJoining!!,
                    salary = _uiState.value.salary.toDouble(),
                    address = _uiState.value.address.trim().takeIf { it.isNotBlank() },
                    emergencyContact = _uiState.value.emergencyContact.trim().takeIf { it.isNotBlank() },
//                    aadharNumber = _uiState.value.aadharNumber.trim().takeIf { it.isNotBlank() },
//                    panNumber = _uiState.value.panNumber.trim().takeIf { it.isNotBlank() },
                    status = WorkerStatus.ACTIVE,
                    rating = 0.0,
                    completedJobs = 0,
                    activeJobs = 0,
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now()
                )

                val result = createWorkerUseCase(worker)

                withContext(Dispatchers.Main) {
                    result.fold(
                        onSuccess = { workerId ->
                            _uiEvent.emit(CreateWorkerUiEvent.WorkerCreated(workerId))
                        },
                        onFailure = { error ->
                            _uiEvent.emit(
                                CreateWorkerUiEvent.ShowError(
                                    error.message ?: "Failed to create worker"
                                )
                            )
                        }
                    )
                }
            }
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

        if (state.role == null) {
            _uiState.update { it.copy(roleError = "Role is required") }
            isValid = false
        }

        if (state.dateOfJoining == null) {
            _uiState.update { it.copy(dateError = "Date of joining is required") }
            isValid = false
        }

        if (state.salary.isBlank()) {
            _uiState.update { it.copy(salaryError = "Salary is required") }
            isValid = false
        } else {
            state.salary.toDoubleOrNull()?.let { salary ->
                if (salary <= 0) {
                    _uiState.update { it.copy(salaryError = "Salary must be greater than 0") }
                    isValid = false
                }
            } ?: run {
                _uiState.update { it.copy(salaryError = "Invalid salary amount") }
                isValid = false
            }
        }

        return isValid
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}

data class CreateWorkerUiState(
    val isLoading: Boolean = false,
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val role: WorkerRole? = null,
    val specialization: String = "",
    val dateOfJoining: LocalDate? = null,
    val salary: String = "",
    val address: String = "",
    val emergencyContact: String = "",
    val aadharNumber: String = "",
    val panNumber: String = "",
    val nameError: String? = null,
    val phoneError: String? = null,
    val emailError: String? = null,
    val roleError: String? = null,
    val dateError: String? = null,
    val salaryError: String? = null
)

sealed class CreateWorkerUiEvent {
    data class WorkerCreated(val workerId: Long) : CreateWorkerUiEvent()
    data object NavigateBackInstantly : CreateWorkerUiEvent()
    data class ShowError(val message: String) : CreateWorkerUiEvent()
}