package com.undef.prowallet.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.undef.prowallet.data.AppRepository
import com.undef.prowallet.data.ProWalletDatabase
import com.undef.prowallet.domain.Purchase
import com.undef.prowallet.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

data class HomeUiState(
    val userName: String = "",
    val totalMonthlySpend: Double = 0.0,
    val monthlyBudget: Double = 0.0,
    val remaining: Double = 0.0,
    val percentageVsLastMonth: Int = 0,
    val recentPurchases: List<Purchase> = emptyList(),
    val budgetProgress: Float = 0f,
    val budgetPercent: Int = 0
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppRepository(application)
    private val sessionManager = SessionManager(application)
    private val userDao = ProWalletDatabase.getInstance(application).userDao()

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val email = sessionManager.email.first()
            if (email != null) {
                val user = userDao.getUserByEmail(email)
                if (user != null) {
                    _uiState.value = _uiState.value.copy(userName = user.name)
                }
            }
        }
        viewModelScope.launch {
            sessionManager.monthlyBudget.collect { budget ->
                val spent = _uiState.value.totalMonthlySpend
                _uiState.value = _uiState.value.copy(
                    monthlyBudget = budget,
                    remaining = budget - spent,
                    budgetProgress = safeProgress(spent, budget),
                    budgetPercent = safePercent(spent, budget)
                )
            }
        }
        viewModelScope.launch {
            repository.seedDefaultCategories()
            repository.purchasesFlow.collect { purchases ->
                val now = Calendar.getInstance()
                val spent = purchases
                    .filter { it.isCurrentMonth(now) }
                    .sumOf { it.totalAmount }
                val budget = _uiState.value.monthlyBudget
                _uiState.value = _uiState.value.copy(
                    recentPurchases = purchases.take(3),
                    totalMonthlySpend = spent,
                    remaining = budget - spent,
                    budgetProgress = safeProgress(spent, budget),
                    budgetPercent = safePercent(spent, budget)
                )
            }
        }
    }

    fun setBudget(amount: Double) {
        viewModelScope.launch { sessionManager.saveBudget(amount) }
    }

    private fun safeProgress(spent: Double, budget: Double): Float =
        if (budget > 0) (spent / budget).toFloat().coerceIn(0f, 1f) else 0f

    private fun safePercent(spent: Double, budget: Double): Int =
        if (budget > 0) ((spent / budget) * 100).toInt().coerceAtLeast(0) else 0

    private fun Purchase.isCurrentMonth(now: Calendar): Boolean {
        return try {
            val cal = Calendar.getInstance()
            cal.time = java.text.SimpleDateFormat("MM/dd/yy", java.util.Locale.getDefault()).parse(date)!!
            cal.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                cal.get(Calendar.MONTH) == now.get(Calendar.MONTH)
        } catch (e: Exception) { false }
    }
}
