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

data class HomeUiState(
    val userName: String = "",
    val totalMonthlySpend: Double = 0.0,
    val monthlyBudget: Double = 0.0,
    val remaining: Double = 0.0,
    val percentageVsLastMonth: Int = 0,
    val recentPurchases: List<Purchase> = emptyList()
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
            repository.seedDefaultCategories()
            repository.purchasesFlow.collect { purchases ->
                val totalSpent = purchases.sumOf { it.totalAmount }
                val budget = _uiState.value.monthlyBudget
                _uiState.value = _uiState.value.copy(
                    recentPurchases = purchases.take(3),
                    totalMonthlySpend = totalSpent,
                    remaining = budget - totalSpent
                )
            }
        }
    }
}
