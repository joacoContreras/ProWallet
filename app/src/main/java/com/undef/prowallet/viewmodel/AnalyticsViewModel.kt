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

data class AnalyticsUiState(
    val totalSpentMonth: Double = 0.0,
    val percentageVsLastMonth: Int = 0,
    val highestSpend: Double = 0.0,
    val highestSpendStore: String = "",
    val averagePurchase: Double = 0.0,
    val totalTransactions: Int = 0,
    val mostPurchasedProducts: List<Pair<String, Int>> = emptyList(),
    val monthlyTrend: List<Pair<String, Float>> = emptyList(),
    val topStores: List<Triple<String, Double, Float>> = emptyList()
)

class AnalyticsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        AnalyticsUiState(
            percentageVsLastMonth = MockRepository.percentageVsLastMonth,
            monthlyTrend = MockRepository.monthlyTrend,
            topStores = MockRepository.topStores
        )
    )
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            PurchaseRepository.purchases.collect { purchases ->
                val highest = purchases.maxByOrNull { it.totalAmount }
                val totalSpent = purchases.sumOf { it.totalAmount }
                val productCounts = purchases.flatMap { it.products }
                    .groupBy { it.name }
                    .mapValues { it.value.size }
                    .toList()
                    .sortedByDescending { it.second }
                    .take(5)
                _uiState.value = _uiState.value.copy(
                    totalSpentMonth = totalSpent,
                    highestSpend = highest?.totalAmount ?: 0.0,
                    highestSpendStore = highest?.storeName ?: "",
                    averagePurchase = if (purchases.isNotEmpty()) purchases.map { it.totalAmount }.average() else 0.0,
                    totalTransactions = purchases.size,
                    mostPurchasedProducts = productCounts
                )
            }
        }
    }
}
