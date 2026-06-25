package com.undef.prowallet.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.undef.prowallet.data.AppRepository
import com.undef.prowallet.domain.Purchase
import com.undef.prowallet.util.StringSimilarity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ProductComparisonSummary(
    val name: String,
    val storeCount: Int,
    val minPrice: Double,
    val maxPrice: Double,
    val averagePrice: Double
)

data class StorePriceComparison(
    val storeName: String,
    val productName: String,
    val price: Double,
    val date: String,
    val timestampMs: Long,
    val isCheapest: Boolean = false,
    val isMostExpensive: Boolean = false,
    val differencePercentage: Double = 0.0
)

data class PriceComparisonUiState(
    val uniqueProducts: List<ProductComparisonSummary> = emptyList(),
    val filteredProducts: List<ProductComparisonSummary> = emptyList(),
    val selectedProduct: ProductComparisonSummary? = null,
    val comparisons: List<StorePriceComparison> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false
)

class PriceComparisonViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppRepository(application)

    private val _uiState = MutableStateFlow(PriceComparisonUiState())
    val uiState: StateFlow<PriceComparisonUiState> = _uiState.asStateFlow()

    private var allPurchases: List<Purchase> = emptyList()

    init {
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            repository.purchasesFlow.collect { purchases ->
                allPurchases = purchases
                updateProductsList(purchases)
            }
        }
    }

    private fun updateProductsList(purchases: List<Purchase>) {
        viewModelScope.launch(Dispatchers.Default) {
            val allProducts = purchases.flatMap { purchase ->
                purchase.products.map { it to purchase }
            }

            val uniqueNames = allProducts.map { it.first.name.trim() }.distinctBy { it.lowercase() }

            val summaries = uniqueNames.map { uniqueName ->
                val matches = allProducts.filter { (prod, _) ->
                    StringSimilarity.calculateSimilarity(prod.name, uniqueName) >= 0.8
                }
                val prices = matches.map { it.first.price }
                val storesCount = matches.map { it.second.storeName }.distinct().size

                ProductComparisonSummary(
                    name = uniqueName,
                    storeCount = storesCount,
                    minPrice = prices.minOrNull() ?: 0.0,
                    maxPrice = prices.maxOrNull() ?: 0.0,
                    averagePrice = prices.average()
                )
            }.sortedByDescending { it.storeCount }

            withContext(Dispatchers.Main) {
                val currentSelected = _uiState.value.selectedProduct
                val newSelected = currentSelected?.let { sel ->
                    summaries.find { it.name.equals(sel.name, ignoreCase = true) }
                }

                _uiState.value = _uiState.value.copy(
                    uniqueProducts = summaries,
                    filteredProducts = filterSummaries(summaries, _uiState.value.searchQuery),
                    selectedProduct = newSelected,
                    comparisons = newSelected?.let { computeComparisons(it.name, allPurchases) } ?: emptyList(),
                    isLoading = false
                )
            }
        }
    }

    fun searchProducts(query: String) {
        val filtered = filterSummaries(_uiState.value.uniqueProducts, query)
        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            filteredProducts = filtered
        )
    }

    private fun filterSummaries(list: List<ProductComparisonSummary>, query: String): List<ProductComparisonSummary> {
        if (query.isBlank()) return list
        return list.filter { it.name.contains(query, ignoreCase = true) }
    }

    fun selectProduct(summary: ProductComparisonSummary?) {
        if (summary == null) {
            _uiState.value = _uiState.value.copy(
                selectedProduct = null,
                comparisons = emptyList()
            )
            return
        }

        viewModelScope.launch(Dispatchers.Default) {
            val comparisons = computeComparisons(summary.name, allPurchases)
            withContext(Dispatchers.Main) {
                _uiState.value = _uiState.value.copy(
                    selectedProduct = summary,
                    comparisons = comparisons
                )
            }
        }
    }

    private fun computeComparisons(productName: String, purchases: List<Purchase>): List<StorePriceComparison> {
        val allProducts = purchases.flatMap { purchase ->
            purchase.products.map { it to purchase }
        }

        // 1. Filter products with similarity >= 80%
        val matches = allProducts.filter { (prod, _) ->
            StringSimilarity.calculateSimilarity(prod.name, productName) >= 0.8
        }

        // 2. Group by store
        val groupedByStore = matches.groupBy { it.second.storeName }

        // 3. For each store, find the most recent purchased item
        val storeComparisons = groupedByStore.map { (storeName, items) ->
            val mostRecent = items.maxByOrNull { it.second.timestampMs }!!
            StorePriceComparison(
                storeName = storeName,
                productName = mostRecent.first.name,
                price = mostRecent.first.price,
                date = mostRecent.second.date,
                timestampMs = mostRecent.second.timestampMs
            )
        }.sortedBy { it.price } // Sort by price ascending (cheapest first)

        if (storeComparisons.isEmpty()) return emptyList()

        val cheapestPrice = storeComparisons.first().price

        return storeComparisons.mapIndexed { index, comp ->
            comp.copy(
                isCheapest = index == 0,
                isMostExpensive = storeComparisons.size > 1 && index == storeComparisons.lastIndex,
                differencePercentage = if (cheapestPrice > 0) {
                    ((comp.price - cheapestPrice) / cheapestPrice) * 100
                } else {
                    0.0
                }
            )
        }
    }
}
