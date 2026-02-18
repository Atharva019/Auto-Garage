package com.autogarage.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autogarage.domain.model.Worker
import com.autogarage.domain.model.WorkerRole
import com.autogarage.domain.usecase.worker.GetWorkerByIdUseCase
import com.autogarage.domain.usecase.worker.UpdateWorkerUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class EditWorkerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getWorkerByIdUseCase: GetWorkerByIdUseCase,
    private val updateWorkerUseCase: UpdateWorkerUseCase
) : ViewModel() {

    private val workerId: Long = savedStateHandle.get<Long>("workerId")?: 0L

    private val _uiState = MutableStateFlow(EditWorkerUiState())
    val uiState: StateFlow<EditWorkerUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<EditWorkerUiEvent>()
    val uiEvent: SharedFlow<EditWorkerUiEvent> = _uiEvent.asSharedFlow()

    private var originalWorker: Worker? = null

    init {
        loadWorker()
    }

    private fun loadWorker() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            getWorkerByIdUseCase(workerId).first()?.let { worker ->
                originalWorker = worker
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        name = worker.name,
                        phone = worker.phone,
                        email = worker.email ?: "",
                        role = worker.role,
                        specialization = worker.specialization ?: "",
                        dateOfJoining = worker.dateOfJoining,
                        salary = worker.salary.toString(),
                        address = worker.address ?: "",
                        emergencyContact = worker.emergencyContact ?: "",
                        //aadharNumber = worker.aadharNumber ?: ""
                    )
                }
            } ?: run {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Worker not found"
                    )
                }
            }
        }
    }

    fun onNameChange(name: String) {
        _uiState.update { it.copy(name = name, nameError = null) }
    }

    fun onPhoneChange(phone: String) {
        val filtered = phone.filter { it.isDigit() }
        if (filtered.length <= 10) {
            _uiState.update { it.copy(phone = filtered, phoneError = null) }
        }
    }

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, emailError = null) }
    }

    fun onRoleChange(role: WorkerRole) {
        _uiState.update { it.copy(role = role, roleError = null) }
    }

    fun onSpecializationChange(specialization: String) {
        _uiState.update { it.copy(specialization = specialization) }
    }

    fun onDateOfJoiningChange(date: LocalDate) {
        _uiState.update { it.copy(dateOfJoining = date, dateError = null) }
    }

    fun onSalaryChange(salary: String) {
        val filtered = salary.filter { it.isDigit() || it == '.' }
        _uiState.update { it.copy(salary = filtered, salaryError = null) }
    }

    fun onAddressChange(address: String) {
        _uiState.update { it.copy(address = address) }
    }

    fun onEmergencyContactChange(contact: String) {
        val filtered = contact.filter { it.isDigit() }
        if (filtered.length <= 10) {
            _uiState.update { it.copy(emergencyContact = filtered) }
        }
    }

    fun onSaveClick() {
        if (!validateForm()) return

        val worker = originalWorker ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            // ✅ INSTANT FEEDBACK: Navigate immediately
            _uiEvent.emit(EditWorkerUiEvent.NavigateBackInstantly)

            withContext(Dispatchers.IO) {
                val updatedWorker = worker.copy(
                    name = _uiState.value.name.trim(),
                    phone = _uiState.value.phone.trim(),
                    email = _uiState.value.email.trim().takeIf { it.isNotBlank() },
                    role = _uiState.value.role!!,
                    specialization = _uiState.value.specialization.trim().takeIf { it.isNotBlank() },
                    dateOfJoining = _uiState.value.dateOfJoining!!,
                    salary = _uiState.value.salary.toDouble(),
                    address = _uiState.value.address.trim().takeIf { it.isNotBlank() },
                    emergencyContact = _uiState.value.emergencyContact.trim().takeIf { it.isNotBlank() }
                    //aadharNumber = _uiState.value.aadharNumber.trim().takeIf { it.isNotBlank() }
                )

                val result = updateWorkerUseCase(updatedWorker)

                withContext(Dispatchers.Main) {
                    result.fold(
                        onSuccess = {
                            _uiEvent.emit(EditWorkerUiEvent.WorkerUpdated)
                        },
                        onFailure = { error ->
                            _uiEvent.emit(
                                EditWorkerUiEvent.ShowError(
                                    error.message ?: "Failed to update worker"
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

data class EditWorkerUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val role: WorkerRole? = null,
    val specialization: String = "",
    val dateOfJoining: LocalDate? = null,
    val salary: String = "",
    val address: String = "",
    val emergencyContact: String = "",
    //val aadharNumber: String = "",
    val nameError: String? = null,
    val phoneError: String? = null,
    val emailError: String? = null,
    val roleError: String? = null,
    val dateError: String? = null,
    val salaryError: String? = null,
    val error: String? = null
)

sealed class EditWorkerUiEvent {
    data object WorkerUpdated : EditWorkerUiEvent()
    data object NavigateBackInstantly : EditWorkerUiEvent()
    data class ShowError(val message: String) : EditWorkerUiEvent()
}