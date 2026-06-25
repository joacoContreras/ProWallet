package com.undef.prowallet.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session")

class SessionManager(private val context: Context) {

    companion object {
        private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        private val EMAIL = stringPreferencesKey("email")
        private val LAST_EMAIL = stringPreferencesKey("last_email")
        private val MONTHLY_BUDGET = doublePreferencesKey("monthly_budget")
        private val MONTHLY_INCOME = doublePreferencesKey("monthly_income")
        private val SAVINGS_PERCENTAGE = androidx.datastore.preferences.core.floatPreferencesKey("savings_percentage")
        private val SAVINGS_METHOD = stringPreferencesKey("savings_method")
        private val SAVINGS_FREQUENCY = stringPreferencesKey("savings_frequency")
        private val SAVINGS_FIXED_AMOUNT = doublePreferencesKey("savings_fixed_amount")
        private val DARK_MODE = booleanPreferencesKey("dark_mode")
        private val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        private val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        private val GROQ_API_KEY = stringPreferencesKey("groq_api_key")
        private val LAST_SYNC_TIME = androidx.datastore.preferences.core.longPreferencesKey("last_sync_time")
    }

    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_LOGGED_IN] ?: false
    }

    val email: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[EMAIL]
    }

    val lastEmail: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[LAST_EMAIL]
    }

    val monthlyBudget: Flow<Double> = context.dataStore.data.map { preferences ->
        preferences[MONTHLY_BUDGET] ?: 0.0
    }

    val monthlyIncome: Flow<Double> = context.dataStore.data.map { preferences ->
        preferences[MONTHLY_INCOME] ?: 0.0
    }

    val savingsPercentage: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[SAVINGS_PERCENTAGE] ?: 10f
    }

    val savingsMethod: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[SAVINGS_METHOD] ?: "Percentage"
    }

    val savingsFrequency: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[SAVINGS_FREQUENCY] ?: "Monthly"
    }

    val savingsFixedAmount: Flow<Double> = context.dataStore.data.map { preferences ->
        preferences[SAVINGS_FIXED_AMOUNT] ?: 0.0
    }

    val darkMode: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[DARK_MODE] ?: false
    }

    val biometricEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[BIOMETRIC_ENABLED] ?: false
    }

    val notificationsEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[NOTIFICATIONS_ENABLED] ?: true
    }

    val groqApiKey: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[GROQ_API_KEY] ?: ""
    }

    suspend fun saveDarkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DARK_MODE] = enabled
        }
    }

    suspend fun saveBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[BIOMETRIC_ENABLED] = enabled
        }
    }

    suspend fun saveNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun saveGroqApiKey(apiKey: String) {
        context.dataStore.edit { preferences ->
            preferences[GROQ_API_KEY] = apiKey
        }
    }

    suspend fun saveBudget(amount: Double) {
        context.dataStore.edit { preferences ->
            preferences[MONTHLY_BUDGET] = amount
        }
    }

    suspend fun saveIncome(amount: Double) {
        context.dataStore.edit { preferences ->
            preferences[MONTHLY_INCOME] = amount
        }
    }

    suspend fun saveSavingsSettings(percentage: Float, method: String, frequency: String, fixedAmount: Double) {
        context.dataStore.edit { preferences ->
            preferences[SAVINGS_PERCENTAGE] = percentage
            preferences[SAVINGS_METHOD] = method
            preferences[SAVINGS_FREQUENCY] = frequency
            preferences[SAVINGS_FIXED_AMOUNT] = fixedAmount
        }
    }

    suspend fun saveSession(email: String) {
        context.dataStore.edit { preferences ->
            preferences[IS_LOGGED_IN] = true
            preferences[EMAIL] = email
            preferences[LAST_EMAIL] = email
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences[IS_LOGGED_IN] = false
            preferences.remove(EMAIL)
            preferences[LAST_SYNC_TIME] = 0L // Reset last sync on logout
        }
    }

    val lastSyncTime: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[LAST_SYNC_TIME] ?: 0L
    }

    suspend fun saveLastSyncTime(time: Long) {
        context.dataStore.edit { preferences ->
            preferences[LAST_SYNC_TIME] = time
        }
    }
}
