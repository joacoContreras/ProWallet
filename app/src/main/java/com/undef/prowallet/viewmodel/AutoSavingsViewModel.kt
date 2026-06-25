package com.undef.prowallet.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.undef.prowallet.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class AutoSavingsUiState(
    val savingsPercentage: Float = 10f,
    val selectedMethod: String = "Percentage",
    val selectedFrequency: String = "Monthly",
    val fixedAmount: Double = 0.0,
    val monthlyIncome: Double = 0.0,
    val isSaved: Boolean = false
) {
    val estimatedMonthlyAmount: Double
        get() = if (selectedMethod == "Fixed Amount") {
            when (selectedFrequency) {
                "Weekly" -> fixedAmount * 4.33
                "Bi-weekly" -> fixedAmount * 2.17
                else -> fixedAmount
            }
        } else {
            monthlyIncome * (savingsPercentage / 100)
        }
}

class AutoSavingsViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application)

    private val _uiState = MutableStateFlow(AutoSavingsUiState())
    val uiState: StateFlow<AutoSavingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                sessionManager.savingsPercentage,
                sessionManager.savingsMethod,
                sessionManager.savingsFrequency,
                sessionManager.savingsFixedAmount,
                sessionManager.monthlyIncome
            ) { pct, method, freq, fixedAmount, income ->
                AutoSavingsUiState(
                    savingsPercentage = pct,
                    selectedMethod = method,
                    selectedFrequency = freq,
                    fixedAmount = fixedAmount,
                    monthlyIncome = income
                )
            }.collect { _uiState.value = it }
        }
    }

    fun onPercentageChange(value: Float) {
        _uiState.value = _uiState.value.copy(savingsPercentage = value)
    }

    fun onMethodChange(value: String) {
        _uiState.value = _uiState.value.copy(selectedMethod = value)
    }

    fun onFrequencyChange(value: String) {
        _uiState.value = _uiState.value.copy(selectedFrequency = value)
    }

    fun onFixedAmountChange(value: Double) {
        _uiState.value = _uiState.value.copy(fixedAmount = value)
    }

    fun saveSettings() {
        val state = _uiState.value
        viewModelScope.launch {
            sessionManager.saveSavingsSettings(
                state.savingsPercentage,
                state.selectedMethod,
                state.selectedFrequency,
                state.fixedAmount
            )
            _uiState.value = state.copy(isSaved = true)
        }
    }

    fun clearSaved() {
        _uiState.value = _uiState.value.copy(isSaved = false)
    }
}
