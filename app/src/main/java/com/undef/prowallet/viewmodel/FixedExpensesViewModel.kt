package com.undef.prowallet.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.undef.prowallet.data.AppRepository
import com.undef.prowallet.data.FixedExpenseEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FixedExpensesUiState(
    val expenses: List<FixedExpenseEntity> = emptyList(),
    val totalMonthly: Double = 0.0
)

class FixedExpensesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppRepository(application)

    private val _uiState = MutableStateFlow(FixedExpensesUiState())
    val uiState: StateFlow<FixedExpensesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.fixedExpensesFlow.collect { expenses ->
                val monthly = expenses.sumOf { expense ->
                    when (expense.frequency) {
                        "yearly" -> expense.amount / 12.0
                        "weekly" -> expense.amount * 4.33
                        else -> expense.amount
                    }
                }
                _uiState.value = FixedExpensesUiState(expenses = expenses, totalMonthly = monthly)
            }
        }
    }

    fun addExpense(name: String, amount: Double, category: String, frequency: String) {
        viewModelScope.launch { repository.addFixedExpense(name, amount, category, frequency) }
    }

    fun updateExpense(id: Int, name: String, amount: Double, category: String, frequency: String) {
        viewModelScope.launch { repository.updateFixedExpense(id, name, amount, category, frequency) }
    }

    fun deleteExpense(id: Int) {
        viewModelScope.launch { repository.deleteFixedExpense(id) }
    }
}
