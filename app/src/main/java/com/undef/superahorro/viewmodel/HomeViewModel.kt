package com.undef.superahorro.viewmodel

import androidx.lifecycle.ViewModel
import com.undef.superahorro.data.MockRepository
import com.undef.superahorro.domain.Purchase
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

    private fun loadData() {
        val purchases = MockRepository.mockPurchases
        val highest = purchases.maxByOrNull { it.totalAmount }

        _uiState.value = HomeUiState(
            userName = MockRepository.currentUser.fullName.split(" ").first(),
            totalMonthlySpend = MockRepository.totalMonthlySpend,
            monthlyBudget = MockRepository.monthlyBudget,
            remaining = MockRepository.remaining,
            percentageVsLastMonth = MockRepository.percentageVsLastMonth,
            recentPurchases = purchases.take(3),
            allPurchases = purchases,
            topStores = MockRepository.topStores,
            monthlyTrend = MockRepository.monthlyTrend,
            totalSpentMonth = 1240.50,
            highestSpend = highest?.totalAmount ?: 0.0,
            highestSpendStore = highest?.storeName ?: "",
            averagePurchase = purchases.map { it.totalAmount }.average(),
            totalTransactions = 27
        )
    }
}
