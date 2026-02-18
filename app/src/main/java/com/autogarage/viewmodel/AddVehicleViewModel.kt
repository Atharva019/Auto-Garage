package com.autogarage.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autogarage.domain.model.FuelType
import com.autogarage.domain.model.TransmissionType
import com.autogarage.domain.usecase.vehicle.AddVehicleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddVehicleViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val addVehicleUseCase: AddVehicleUseCase
) : ViewModel() {

    private val customerId: Long = savedStateHandle.get<Long>("customerId") ?: 0L

    private val _uiState = MutableStateFlow(AddVehicleUiState())
    val uiState: StateFlow<AddVehicleUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<AddVehicleUiEvent>()
    val uiEvent: SharedFlow<AddVehicleUiEvent> = _uiEvent.asSharedFlow()

    fun onRegistrationNumberChange(regNumber: String) {
        // Convert to uppercase and remove spaces
        val filtered = regNumber.uppercase().filter { it.isLetterOrDigit() }
        _uiState.update {
            it.copy(
                registrationNumber = filtered,
                registrationNumberError = null
            )
        }
    }

    fun onMakeChange(make: String) {
        _uiState.update { it.copy(make = make, makeError = null) }
    }

    fun onModelChange(model: String) {
        _uiState.update { it.copy(model = model, modelError = null) }
    }

    fun onYearChange(year: String) {
        val filtered = year.filter { it.isDigit() }
        if (filtered.length <= 4) {
            _uiState.update { it.copy(year = filtered, yearError = null) }
        }
    }

    fun onColorChange(color: String) {
        _uiState.update { it.copy(color = color) }
    }

    fun onEngineNumberChange(engineNumber: String) {
        _uiState.update { it.copy(engineNumber = engineNumber) }
    }

    fun onChassisNumberChange(chassisNumber: String) {
        _uiState.update { it.copy(chassisNumber = chassisNumber) }
    }

    fun onFuelTypeSelected(fuelType: FuelType?) {
        _uiState.update { it.copy(fuelType = fuelType) }
    }

    fun onTransmissionSelected(transmission: TransmissionType?) {
        _uiState.update { it.copy(transmission = transmission) }
    }

    fun onCurrentKilometersChange(km: String) {
        val filtered = km.filter { it.isDigit() }
        _uiState.update { it.copy(currentKilometers = filtered) }
    }

    fun onInsuranceExpiryChange(date: String) {
        _uiState.update { it.copy(insuranceExpiryDate = date) }
    }

    fun onPucExpiryChange(date: String) {
        _uiState.update { it.copy(pucExpiryDate = date) }
    }

    fun onNotesChange(notes: String) {
        _uiState.update { it.copy(notes = notes) }
    }

    fun onSaveClick() {
        if (!validateForm()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            val result = addVehicleUseCase(
                AddVehicleUseCase.Params(
                    customerId = customerId,
                    registrationNumber = _uiState.value.registrationNumber.trim(),
                    make = _uiState.value.make.trim(),
                    model = _uiState.value.model.trim(),
                    year = _uiState.value.year.toInt(),
                    color = _uiState.value.color.trim().takeIf { it.isNotBlank() },
                    fuelType = _uiState.value.fuelType,
                    transmission = _uiState.value.transmission,
                    currentKilometers = _uiState.value.currentKilometers.toIntOrNull() ?: 0
                )
            )

            result.fold(
                onSuccess = { vehicleId ->
                    _uiEvent.emit(AddVehicleUiEvent.VehicleAdded(vehicleId))
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isSaving = false) }
                    _uiEvent.emit(
                        AddVehicleUiEvent.ShowError(
                            error.message ?: "Failed to add vehicle"
                        )
                    )
                }
            )
        }
    }

    private fun validateForm(): Boolean {
        val state = _uiState.value
        var isValid = true

        if (state.registrationNumber.isBlank()) {
            _uiState.update { it.copy(registrationNumberError = "Registration number is required") }
            isValid = false
        } else if (state.registrationNumber.length < 6) {
            _uiState.update { it.copy(registrationNumberError = "Invalid registration number") }
            isValid = false
        }

        if (state.make.isBlank()) {
            _uiState.update { it.copy(makeError = "Make is required") }
            isValid = false
        }

        if (state.model.isBlank()) {
            _uiState.update { it.copy(modelError = "Model is required") }
            isValid = false
        }

        if (state.year.isBlank()) {
            _uiState.update { it.copy(yearError = "Year is required") }
            isValid = false
        } else {
            val yearInt = state.year.toIntOrNull()
            if (yearInt == null || yearInt < 1900 || yearInt > 2100) {
                _uiState.update { it.copy(yearError = "Invalid year") }
                isValid = false
            }
        }

        return isValid
    }
}

data class AddVehicleUiState(
    val registrationNumber: String = "",
    val make: String = "",
    val model: String = "",
    val year: String = "",
    val color: String = "",
    val engineNumber: String = "",
    val chassisNumber: String = "",
    val fuelType: FuelType? = null,
    val transmission: TransmissionType? = null,
    val currentKilometers: String = "0",
    val insuranceExpiryDate: String = "",
    val pucExpiryDate: String = "",
    val notes: String = "",
    val registrationNumberError: String? = null,
    val makeError: String? = null,
    val modelError: String? = null,
    val yearError: String? = null,
    val isSaving: Boolean = false
)

sealed class AddVehicleUiEvent {
    data class VehicleAdded(val vehicleId: Long) : AddVehicleUiEvent()
    data class ShowError(val message: String) : AddVehicleUiEvent()
}
