package com.undef.prowallet.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.undef.prowallet.data.AppRepository
import com.undef.prowallet.util.isCurrentMonth
import com.undef.prowallet.util.isInMonth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

data class StoreEntry(
    val name: String,
    val totalAmount: Double,
    val transactionCount: Int,
    val percentageVsLastMonth: Int? = null
)

data class CategoryDistEntry(
    val name: String,
    val percentage: Float
)

data class TopStoresUiState(
    val totalSpentMonth: Double = 0.0,
    val percentageVsLastMonth: Int = 0,
    val stores: List<StoreEntry> = emptyList(),
    val categoryDistribution: List<CategoryDistEntry> = emptyList()
)

class TopStoresViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppRepository(application)

    private val _uiState = MutableStateFlow(TopStoresUiState())
    val uiState: StateFlow<TopStoresUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.purchasesFlow.collect { purchases ->
                val current = purchases.filter { it.isCurrentMonth() }
                val totalSpent = current.sumOf { it.totalAmount }

                val now = Calendar.getInstance()
                val thisMonth = now.get(Calendar.MONTH)
                val thisYear = now.get(Calendar.YEAR)
                val lastMonth = if (thisMonth == 0) 11 else thisMonth - 1
                val lastMonthYear = if (thisMonth == 0) thisYear - 1 else thisYear
                val lastMonthPurchases = purchases.filter { it.isInMonth(lastMonthYear, lastMonth) }
                val totalLastMonth = lastMonthPurchases.sumOf { it.totalAmount }
                val percentageVsLastMonth = if (totalLastMonth > 0)
                    (((totalSpent - totalLastMonth) / totalLastMonth) * 100).toInt() else 0
                val lastMonthByStore = lastMonthPurchases
                    .groupBy { it.storeName }
                    .mapValues { (_, list) -> list.sumOf { it.totalAmount } }

                val stores = current
                    .groupBy { it.storeName }
                    .map { (name, list) ->
                        val previous = lastMonthByStore[name]
                        val storeTotal = list.sumOf { it.totalAmount }
                        StoreEntry(
                            name = name,
                            totalAmount = storeTotal,
                            transactionCount = list.size,
                            percentageVsLastMonth = if (previous != null && previous > 0)
                                (((storeTotal - previous) / previous) * 100).toInt() else null
                        )
                    }
                    .sortedByDescending { it.totalAmount }

                val grandTotal = totalSpent.takeIf { it > 0.0 } ?: 1.0
                val categoryDist = current
                    .groupBy { it.category }
                    .mapValues { (_, list) -> list.sumOf { it.totalAmount } }
                    .map { (cat, amt) -> CategoryDistEntry(cat, (amt / grandTotal).toFloat()) }
                    .sortedByDescending { it.percentage }
                    .take(4)

                _uiState.value = TopStoresUiState(
                    totalSpentMonth = totalSpent,
                    percentageVsLastMonth = percentageVsLastMonth,
                    stores = stores,
                    categoryDistribution = categoryDist
                )
            }
        }
    }
}
