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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class AutoSavingsUiState(
    // Configuración de ahorro
    val savingsPercentage: Float = 10f,
    val selectedMethod: String = "Percentage",
    val selectedFrequency: String = "Monthly",
    val fixedAmount: Double = 0.0,
    val monthlyIncome: Double = 0.0,
    val isSaved: Boolean = false,
    // Meta de ahorro
    val goalTarget: Double = 0.0,
    val goalMonths: Int = 0,
    val goalStartMs: Long = 0L,
    val goalTargetInput: String = "",
    val goalMonthsInput: String = "",
    val goalInputError: Boolean = false,
    // Datos reales de gastos
    val currentMonthlySpending: Double = 0.0,
    val topCategories: List<Pair<String, Double>> = emptyList()
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

    val hasActiveGoal: Boolean get() = goalTarget > 0 && goalMonths > 0
    val monthlyNeeded: Double get() = if (goalMonths > 0) goalTarget / goalMonths else 0.0
    val monthlySavingCapacity: Double get() = (monthlyIncome - currentMonthlySpending).coerceAtLeast(0.0)
    val isOnTrack: Boolean get() = hasActiveGoal && monthlySavingCapacity >= monthlyNeeded

    val elapsedMonths: Int
        get() {
            if (goalStartMs == 0L) return 0
            val elapsed = System.currentTimeMillis() - goalStartMs
            return (elapsed / (30L * 24 * 60 * 60 * 1000)).toInt().coerceAtLeast(0)
        }

    val accumulatedSavings: Double
        get() = monthlySavingCapacity * elapsedMonths

    val progressFraction: Float
        get() {
            if (!hasActiveGoal || goalTarget <= 0) return 0f
            return (accumulatedSavings / goalTarget).toFloat().coerceIn(0f, 1f)
        }

    val monthsRemaining: Int
        get() {
            if (!hasActiveGoal || monthlySavingCapacity <= 0) return goalMonths
            val remaining = ((goalTarget - accumulatedSavings) / monthlySavingCapacity).toInt()
            return remaining.coerceAtLeast(0)
        }
}

class AutoSavingsViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application)
    private val repository = AppRepository(application)

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
            ) { pct, method, freq, fixedAmt, income ->
                _uiState.value.copy(
                    savingsPercentage = pct,
                    selectedMethod = method,
                    selectedFrequency = freq,
                    fixedAmount = fixedAmt,
                    monthlyIncome = income
                )
            }.collect { _uiState.value = it }
        }

        viewModelScope.launch {
            combine(
                sessionManager.savingsGoalTarget,
                sessionManager.savingsGoalMonths,
                sessionManager.savingsGoalStartMs
            ) { target, months, startMs ->
                Triple(target, months, startMs)
            }.collect { (target, months, startMs) ->
                _uiState.value = _uiState.value.copy(
                    goalTarget = target,
                    goalMonths = months,
                    goalStartMs = startMs
                )
            }
        }

        viewModelScope.launch {
            repository.purchasesFlow.collect { purchases ->
                val monthSpend = purchases.filter { it.isCurrentMonth() }.sumOf { it.totalAmount }
                val topCats = purchases
                    .filter { it.isCurrentMonth() }
                    .groupBy { it.category }
                    .mapValues { (_, list) -> list.sumOf { it.totalAmount } }
                    .toList()
                    .sortedByDescending { it.second }
                    .take(3)
                _uiState.value = _uiState.value.copy(
                    currentMonthlySpending = monthSpend,
                    topCategories = topCats
                )
            }
        }
    }

    // ── Configuración de ahorro ───────────────────────────────────────────────

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

    // ── Meta de ahorro ────────────────────────────────────────────────────────

    fun onGoalTargetInputChange(value: String) {
        _uiState.value = _uiState.value.copy(goalTargetInput = value, goalInputError = false)
    }

    fun onGoalMonthsInputChange(value: String) {
        _uiState.value = _uiState.value.copy(goalMonthsInput = value, goalInputError = false)
    }

    fun saveGoal() {
        val state = _uiState.value
        val target = state.goalTargetInput.toDoubleOrNull()
        val months = state.goalMonthsInput.toIntOrNull()
        if (target == null || target <= 0 || months == null || months <= 0) {
            _uiState.value = state.copy(goalInputError = true)
            return
        }
        viewModelScope.launch {
            sessionManager.saveSavingsGoal(target, months)
            _uiState.value = _uiState.value.copy(goalTargetInput = "", goalMonthsInput = "", goalInputError = false)
        }
    }

    fun clearGoal() {
        viewModelScope.launch {
            sessionManager.clearSavingsGoal()
        }
    }
}
