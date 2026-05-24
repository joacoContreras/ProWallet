package com.undef.prowallet.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.undef.prowallet.data.AppRepository
import com.undef.prowallet.domain.Purchase
import com.undef.prowallet.util.isInMonth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.DateFormatSymbols
import java.util.Calendar
import java.util.Locale

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

class AnalyticsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppRepository(application)

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.purchasesFlow.collect { purchases ->
                _uiState.value = computeAnalytics(purchases)
            }
        }
    }

    private fun computeAnalytics(purchases: List<Purchase>): AnalyticsUiState {
        if (purchases.isEmpty()) return AnalyticsUiState()

        val now = Calendar.getInstance()
        val thisMonth = now.get(Calendar.MONTH)
        val thisYear = now.get(Calendar.YEAR)
        val lastMonth = if (thisMonth == 0) 11 else thisMonth - 1
        val lastMonthYear = if (thisMonth == 0) thisYear - 1 else thisYear

        val thisMonthPurchases = purchases.filter { it.isInMonth(thisYear, thisMonth) }
        val lastMonthPurchases = purchases.filter { it.isInMonth(lastMonthYear, lastMonth) }

        val totalThisMonth = thisMonthPurchases.sumOf { it.totalAmount }
        val totalLastMonth = lastMonthPurchases.sumOf { it.totalAmount }
        val percentageVsLastMonth = if (totalLastMonth > 0)
            (((totalThisMonth - totalLastMonth) / totalLastMonth) * 100).toInt() else 0

        val highest = purchases.maxByOrNull { it.totalAmount }
        val productCounts = purchases.flatMap { it.products }
            .groupBy { it.name }
            .mapValues { it.value.size }
            .toList()
            .sortedByDescending { it.second }
            .take(5)

        val storeGrouped = purchases.groupBy { it.storeName }
        val totalAll = purchases.sumOf { it.totalAmount }
        val topStores = storeGrouped.entries
            .map { (store, list) -> Triple(store, list.sumOf { it.totalAmount }, 0f) }
            .sortedByDescending { it.second }
            .take(3)
            .map { (store, total, _) ->
                Triple(store, total, if (totalAll > 0) (total / totalAll).toFloat() else 0f)
            }

        // Monthly trend: last 6 calendar months
        val shortMonths = DateFormatSymbols.getInstance(Locale.getDefault()).shortMonths
        val monthlyTrend = (5 downTo 0).map { monthsAgo ->
            val cal = Calendar.getInstance().also { it.add(Calendar.MONTH, -monthsAgo) }
            val m = cal.get(Calendar.MONTH)
            val y = cal.get(Calendar.YEAR)
            val total = purchases
                .filter { it.isInMonth(y, m) }
                .sumOf { it.totalAmount }
                .toFloat()
            Pair(shortMonths[m], total)
        }

        return AnalyticsUiState(
            totalSpentMonth = totalThisMonth,
            percentageVsLastMonth = percentageVsLastMonth,
            highestSpend = highest?.totalAmount ?: 0.0,
            highestSpendStore = highest?.storeName ?: "",
            averagePurchase = purchases.map { it.totalAmount }.average(),
            totalTransactions = purchases.size,
            mostPurchasedProducts = productCounts,
            monthlyTrend = monthlyTrend,
            topStores = topStores
        )
    }
}
