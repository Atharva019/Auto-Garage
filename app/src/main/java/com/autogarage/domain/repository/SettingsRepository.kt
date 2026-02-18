package com.autogarage.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    // Business Settings
    suspend fun getBusinessName(): String
    suspend fun setBusinessName(name: String)
    suspend fun getBusinessPhone(): String
    suspend fun setBusinessPhone(phone: String)
    suspend fun getBusinessEmail(): String
    suspend fun setBusinessEmail(email: String)
    suspend fun getBusinessAddress(): String
    suspend fun setBusinessAddress(address: String)
    suspend fun getGstNumber(): String
    suspend fun setGstNumber(gstNumber: String)

    // App Settings
    suspend fun getThemeMode(): ThemeMode
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun getCurrency(): String
    suspend fun setCurrency(currency: String)
    suspend fun getDefaultTaxRate(): Double
    suspend fun setDefaultTaxRate(rate: Double)

    // Notification Settings
    suspend fun getLowStockAlertEnabled(): Boolean
    suspend fun setLowStockAlertEnabled(enabled: Boolean)
    suspend fun getJobCompletionNotificationEnabled(): Boolean
    suspend fun setJobCompletionNotificationEnabled(enabled: Boolean)

    // Flow versions for observing changes
    fun observeThemeMode(): Flow<ThemeMode>
    fun observeCurrency(): Flow<String>
}

enum class ThemeMode {
    LIGHT, DARK, SYSTEM
}
