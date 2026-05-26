package com.undef.prowallet.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.undef.prowallet.data.AppRepository
import com.undef.prowallet.util.isCurrentMonth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class StoreEntry(
    val name: String,
    val totalAmount: Double,
    val transactionCount: Int
)

data class CategoryDistEntry(
    val name: String,
    val percentage: Float
)

data class TopStoresUiState(
    val totalSpentMonth: Double = 0.0,
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

                val stores = current
                    .groupBy { it.storeName }
                    .map { (name, list) ->
                        StoreEntry(
                            name = name,
                            totalAmount = list.sumOf { it.totalAmount },
                            transactionCount = list.size
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
                    stores = stores,
                    categoryDistribution = categoryDist
                )
            }
        }
    }
}
