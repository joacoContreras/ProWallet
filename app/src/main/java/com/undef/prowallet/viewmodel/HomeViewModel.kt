package com.undef.prowallet.viewmodel

import androidx.lifecycle.ViewModel
import com.undef.prowallet.data.MockRepository
import com.undef.prowallet.domain.Purchase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class HomeUiState(
    val userName: String = "Martin",
    val totalMonthlySpend: Double = 0.0,
    val monthlyBudget: Double = 0.0,
    val remaining: Double = 0.0,
    val percentageVsLastMonth: Int = 0,
    val recentPurchases: List<Purchase> = emptyList(),
    val allPurchases: List<Purchase> = emptyList(),
    val topStores: List<Triple<String, Double, Float>> = emptyList(),
    val monthlyTrend: List<Pair<String, Float>> = emptyList(),
    val totalSpentMonth: Double = 0.0,
    val highestSpend: Double = 0.0,
    val highestSpendStore: String = "",
    val averagePurchase: Double = 0.0,
    val totalTransactions: Int = 0
)

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun deletePurchase(purchaseId: String) {
        val currentList = _uiState.value.allPurchases
        val newList = currentList.filter { it.id != purchaseId }
        updateStateWithPurchases(newList)
    }

    private fun updateStateWithPurchases(purchases: List<Purchase>) {
        val highest = purchases.maxByOrNull { it.totalAmount }
        val totalSpent = purchases.sumOf { it.totalAmount }
        
        _uiState.value = _uiState.value.copy(
            allPurchases = purchases,
            recentPurchases = purchases.take(3),
            totalMonthlySpend = totalSpent,
            remaining = _uiState.value.monthlyBudget - totalSpent,
            highestSpend = highest?.totalAmount ?: 0.0,
            highestSpendStore = highest?.storeName ?: "",
            averagePurchase = if (purchases.isNotEmpty()) purchases.map { it.totalAmount }.average() else 0.0,
            totalTransactions = purchases.size
        )
    }

    private fun loadData() {
        updateStateWithPurchases(MockRepository.mockPurchases)
        _uiState.value = _uiState.value.copy(
            userName = MockRepository.currentUser.fullName.split(" ").first(),
            monthlyBudget = MockRepository.monthlyBudget,
            percentageVsLastMonth = MockRepository.percentageVsLastMonth,
            topStores = MockRepository.topStores,
            monthlyTrend = MockRepository.monthlyTrend,
            totalSpentMonth = 1240.50
        )
    }
}
