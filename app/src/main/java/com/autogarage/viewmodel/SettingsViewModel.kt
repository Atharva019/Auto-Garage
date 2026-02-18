package com.autogarage.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autogarage.domain.repository.SettingsRepository
import com.autogarage.domain.repository.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<SettingsUiEvent>()
    val uiEvent: SharedFlow<SettingsUiEvent> = _uiEvent.asSharedFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val businessName = settingsRepository.getBusinessName()
                val businessPhone = settingsRepository.getBusinessPhone()
                val businessEmail = settingsRepository.getBusinessEmail()
                val businessAddress = settingsRepository.getBusinessAddress()
                val gstNumber = settingsRepository.getGstNumber()
                val themeMode = settingsRepository.getThemeMode()
                val currency = settingsRepository.getCurrency()
                val defaultTaxRate = settingsRepository.getDefaultTaxRate()
                val lowStockAlert = settingsRepository.getLowStockAlertEnabled()
                val jobCompletionNotification = settingsRepository.getJobCompletionNotificationEnabled()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        businessName = businessName,
                        businessPhone = businessPhone,
                        businessEmail = businessEmail,
                        businessAddress = businessAddress,
                        gstNumber = gstNumber,
                        themeMode = themeMode,
                        currency = currency,
                        defaultTaxRate = defaultTaxRate.toString(),
                        lowStockAlertEnabled = lowStockAlert,
                        jobCompletionNotificationEnabled = jobCompletionNotification
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
                _uiEvent.emit(SettingsUiEvent.ShowError("Failed to load settings"))
            }
        }
    }

    fun onBusinessNameChange(name: String) {
        _uiState.update { it.copy(businessName = name) }
    }

    fun onBusinessPhoneChange(phone: String) {
        val filtered = phone.filter { it.isDigit() }
        if (filtered.length <= 10) {
            _uiState.update { it.copy(businessPhone = filtered) }
        }
    }

    fun onBusinessEmailChange(email: String) {
        _uiState.update { it.copy(businessEmail = email) }
    }

    fun onBusinessAddressChange(address: String) {
        _uiState.update { it.copy(businessAddress = address) }
    }

    fun onGstNumberChange(gstNumber: String) {
        _uiState.update { it.copy(gstNumber = gstNumber) }
    }

    fun onThemeModeChange(mode: ThemeMode) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(mode)
            _uiState.update { it.copy(themeMode = mode) }
            _uiEvent.emit(SettingsUiEvent.ShowMessage("Theme updated"))
        }
    }

    fun onCurrencyChange(currency: String) {
        _uiState.update { it.copy(currency = currency) }
    }

    fun onDefaultTaxRateChange(rate: String) {
        val filtered = rate.filter { it.isDigit() || it == '.' }
        _uiState.update { it.copy(defaultTaxRate = filtered) }
    }

    fun onLowStockAlertToggle(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setLowStockAlertEnabled(enabled)
            _uiState.update { it.copy(lowStockAlertEnabled = enabled) }
        }
    }

    fun onJobCompletionNotificationToggle(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setJobCompletionNotificationEnabled(enabled)
            _uiState.update { it.copy(jobCompletionNotificationEnabled = enabled) }
        }
    }

    fun onSaveBusinessSettings() {
        viewModelScope.launch {
            try {
                settingsRepository.setBusinessName(_uiState.value.businessName)
                settingsRepository.setBusinessPhone(_uiState.value.businessPhone)
                settingsRepository.setBusinessEmail(_uiState.value.businessEmail)
                settingsRepository.setBusinessAddress(_uiState.value.businessAddress)
                settingsRepository.setGstNumber(_uiState.value.gstNumber)

                _uiEvent.emit(SettingsUiEvent.ShowMessage("Business settings saved"))
            } catch (e: Exception) {
                _uiEvent.emit(SettingsUiEvent.ShowError("Failed to save settings"))
            }
        }
    }

    fun onSaveAppSettings() {
        viewModelScope.launch {
            try {
                settingsRepository.setCurrency(_uiState.value.currency)
                val taxRate = _uiState.value.defaultTaxRate.toDoubleOrNull()
                if (taxRate != null && taxRate >= 0 && taxRate <= 100) {
                    settingsRepository.setDefaultTaxRate(taxRate)
                    _uiEvent.emit(SettingsUiEvent.ShowMessage("App settings saved"))
                } else {
                    _uiEvent.emit(SettingsUiEvent.ShowError("Invalid tax rate"))
                }
            } catch (e: Exception) {
                _uiEvent.emit(SettingsUiEvent.ShowError("Failed to save settings"))
            }
        }
    }
}

data class SettingsUiState(
    val isLoading: Boolean = true,
    val businessName: String = "",
    val businessPhone: String = "",
    val businessEmail: String = "",
    val businessAddress: String = "",
    val gstNumber: String = "",
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val currency: String = "₹",
    val defaultTaxRate: String = "18.0",
    val lowStockAlertEnabled: Boolean = true,
    val jobCompletionNotificationEnabled: Boolean = true
)

sealed class SettingsUiEvent {
    data class ShowMessage(val message: String) : SettingsUiEvent()
    data class ShowError(val message: String) : SettingsUiEvent()
}