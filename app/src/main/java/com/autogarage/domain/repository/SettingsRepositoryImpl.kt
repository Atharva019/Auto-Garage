package com.autogarage.domain.repository

import android.content.SharedPreferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val sharedPreferences: SharedPreferences
) : SettingsRepository {

    private val _themeMode = MutableStateFlow(getThemeModeSync())
    private val _currency = MutableStateFlow(getCurrencySync())

    companion object {
        private const val KEY_BUSINESS_NAME = "business_name"
        private const val KEY_BUSINESS_PHONE = "business_phone"
        private const val KEY_BUSINESS_EMAIL = "business_email"
        private const val KEY_BUSINESS_ADDRESS = "business_address"
        private const val KEY_GST_NUMBER = "gst_number"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_CURRENCY = "currency"
        private const val KEY_DEFAULT_TAX_RATE = "default_tax_rate"
        private const val KEY_LOW_STOCK_ALERT = "low_stock_alert"
        private const val KEY_JOB_COMPLETION_NOTIFICATION = "job_completion_notification"
        val TAX_RATE = doublePreferencesKey("tax_rate")
        val BUSINESS_NAME = stringPreferencesKey("business_name")
        val BUSINESS_ADDRESS = stringPreferencesKey("business_address")
        val BUSINESS_PHONE = stringPreferencesKey("business_phone")
        val BUSINESS_EMAIL = stringPreferencesKey("business_email")
        val BUSINESS_GSTIN = stringPreferencesKey("business_gstin")
    }

    // Business Settings
    override suspend fun getBusinessName(): String {
        return sharedPreferences.getString(KEY_BUSINESS_NAME, "GarageMaster") ?: "GarageMaster"
    }

    override suspend fun setBusinessName(name: String) {
        sharedPreferences.edit().putString(KEY_BUSINESS_NAME, name).apply()
    }

    override suspend fun getBusinessPhone(): String {
        return sharedPreferences.getString(KEY_BUSINESS_PHONE, "") ?: ""
    }

    override suspend fun setBusinessPhone(phone: String) {
        sharedPreferences.edit().putString(KEY_BUSINESS_PHONE, phone).apply()
    }

    override suspend fun getBusinessEmail(): String {
        return sharedPreferences.getString(KEY_BUSINESS_EMAIL, "") ?: ""
    }

    override suspend fun setBusinessEmail(email: String) {
        sharedPreferences.edit().putString(KEY_BUSINESS_EMAIL, email).apply()
    }

    override suspend fun getBusinessAddress(): String {
        return sharedPreferences.getString(KEY_BUSINESS_ADDRESS, "") ?: ""
    }

    override suspend fun setBusinessAddress(address: String) {
        sharedPreferences.edit().putString(KEY_BUSINESS_ADDRESS, address).apply()
    }

    override suspend fun getGstNumber(): String {
        return sharedPreferences.getString(KEY_GST_NUMBER, "") ?: ""
    }

    override suspend fun setGstNumber(gstNumber: String) {
        sharedPreferences.edit().putString(KEY_GST_NUMBER, gstNumber).apply()
    }

    // App Settings
    override suspend fun getThemeMode(): ThemeMode {
        return getThemeModeSync()
    }

    private fun getThemeModeSync(): ThemeMode {
        val mode = sharedPreferences.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name)
            ?: ThemeMode.SYSTEM.name
        return ThemeMode.valueOf(mode)
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        sharedPreferences.edit().putString(KEY_THEME_MODE, mode.name).apply()
        _themeMode.value = mode
    }

    override suspend fun getCurrency(): String {
        return getCurrencySync()
    }

    private fun getCurrencySync(): String {
        return sharedPreferences.getString(KEY_CURRENCY, "₹") ?: "₹"
    }

    override suspend fun setCurrency(currency: String) {
        sharedPreferences.edit().putString(KEY_CURRENCY, currency).apply()
        _currency.value = currency
    }

    override suspend fun getDefaultTaxRate(): Double {
        return sharedPreferences.getFloat(KEY_DEFAULT_TAX_RATE, 18.0f).toDouble()
    }

    override suspend fun setDefaultTaxRate(rate: Double) {
        sharedPreferences.edit().putFloat(KEY_DEFAULT_TAX_RATE, rate.toFloat()).apply()
    }

    // Notification Settings
    override suspend fun getLowStockAlertEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_LOW_STOCK_ALERT, true)
    }

    override suspend fun setLowStockAlertEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_LOW_STOCK_ALERT, enabled).apply()
    }

    override suspend fun getJobCompletionNotificationEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_JOB_COMPLETION_NOTIFICATION, true)
    }

    override suspend fun setJobCompletionNotificationEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_JOB_COMPLETION_NOTIFICATION, enabled).apply()
    }

    // Flow versions
    override fun observeThemeMode(): Flow<ThemeMode> = _themeMode.asStateFlow()
    override fun observeCurrency(): Flow<String> = _currency.asStateFlow()
}