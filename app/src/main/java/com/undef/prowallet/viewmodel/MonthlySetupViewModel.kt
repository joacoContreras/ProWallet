package com.undef.prowallet.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.undef.prowallet.data.AppRepository
import com.undef.prowallet.util.SessionManager
import com.undef.prowallet.util.isCurrentMonth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MonthlySetupUiState(
    val monthlyIncome: String = "",
    val monthlyBudget: String = "",
    val isSaved: Boolean = false,
    val categorySpend: List<Pair<String, Double>> = emptyList(),
    val totalSpentThisMonth: Double = 0.0
)

class MonthlySetupViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application)
    private val repository = AppRepository(application)

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
        viewModelScope.launch {
            repository.purchasesFlow.collect { purchases ->
                val current = purchases.filter { it.isCurrentMonth() }
                val byCategory = current
                    .groupBy { it.category }
                    .mapValues { (_, list) -> list.sumOf { it.totalAmount } }
                    .toList()
                    .sortedByDescending { it.second }
                _uiState.value = _uiState.value.copy(
                    categorySpend = byCategory,
                    totalSpentThisMonth = current.sumOf { it.totalAmount }
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
