package com.undef.prowallet.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.undef.prowallet.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MonthlySetupUiState(
    val monthlyIncome: String = "",
    val monthlyBudget: String = "",
    val isSaved: Boolean = false
)

class MonthlySetupViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application)

    private val _uiState = MutableStateFlow(MonthlySetupUiState())
    val uiState: StateFlow<MonthlySetupUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            sessionManager.monthlyIncome.collect { income ->
                _uiState.value = _uiState.value.copy(
                    monthlyIncome = if (income > 0) String.format("%.2f", income) else ""
                )
            }
        }
        viewModelScope.launch {
            sessionManager.monthlyBudget.collect { budget ->
                _uiState.value = _uiState.value.copy(
                    monthlyBudget = if (budget > 0) String.format("%.2f", budget) else ""
                )
            }
        }
    }

    fun onIncomeChange(value: String) {
        _uiState.value = _uiState.value.copy(monthlyIncome = value)
    }

    fun onBudgetChange(value: String) {
        _uiState.value = _uiState.value.copy(monthlyBudget = value)
    }

    fun save() {
        val state = _uiState.value
        viewModelScope.launch {
            state.monthlyIncome.toDoubleOrNull()?.let { sessionManager.saveIncome(it) }
            val budgetValue = state.monthlyBudget.toDoubleOrNull()
                ?: state.monthlyIncome.toDoubleOrNull()
                ?: 0.0
            sessionManager.saveBudget(budgetValue)
            _uiState.value = state.copy(isSaved = true)
        }
    }

    fun clearSaved() {
        _uiState.value = _uiState.value.copy(isSaved = false)
    }
}
