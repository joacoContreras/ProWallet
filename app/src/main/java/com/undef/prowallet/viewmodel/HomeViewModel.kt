package com.undef.prowallet.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.undef.prowallet.data.MockRepository
import com.undef.prowallet.data.PurchaseRepository
import com.undef.prowallet.domain.Purchase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val userName: String = "Martin",
    val totalMonthlySpend: Double = 0.0,
    val monthlyBudget: Double = 0.0,
    val remaining: Double = 0.0,
    val percentageVsLastMonth: Int = 0,
    val recentPurchases: List<Purchase> = emptyList()
)

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        HomeUiState(
            userName = MockRepository.currentUser.fullName.split(" ").first(),
            monthlyBudget = MockRepository.monthlyBudget,
            percentageVsLastMonth = MockRepository.percentageVsLastMonth
        )
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            PurchaseRepository.purchases.collect { purchases ->
                val totalSpent = purchases.sumOf { it.totalAmount }
                _uiState.value = _uiState.value.copy(
                    recentPurchases = purchases.take(3),
                    totalMonthlySpend = totalSpent,
                    remaining = _uiState.value.monthlyBudget - totalSpent
                )
            }
        }
    }
}
