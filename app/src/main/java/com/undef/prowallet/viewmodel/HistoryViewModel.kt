package com.undef.prowallet.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.undef.prowallet.data.AppRepository
import com.undef.prowallet.domain.Purchase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Locale

data class HistoryFilter(
    val storeQuery: String = "",
    val category: String? = null,
    val dateFromMs: Long? = null,
    val dateToMs: Long? = null,
    val minAmount: Double? = null,
    val maxAmount: Double? = null
) {
    val activeCount: Int
        get() = listOf(
            storeQuery.isNotBlank(),
            category != null,
            dateFromMs != null,
            dateToMs != null,
            minAmount != null,
            maxAmount != null
        ).count { it }
}

data class HistoryUiState(
    val allPurchases: List<Purchase> = emptyList(),
    val filteredPurchases: List<Purchase> = emptyList(),
    val availableCategories: List<String> = emptyList(),
    val filter: HistoryFilter = HistoryFilter()
)

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppRepository(application)

    private val _filter = MutableStateFlow(HistoryFilter())

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(repository.purchasesFlow, repository.categoriesFlow, _filter) { purchases, categories, filter ->
                Triple(purchases, categories, filter)
            }.collect { (purchases, categories, filter) ->
                _uiState.value = HistoryUiState(
                    allPurchases = purchases,
                    filteredPurchases = purchases.filter { it.matches(filter) },
                    availableCategories = categories.map { it.name },
                    filter = filter
                )
            }
        }
    }

    private fun Purchase.matches(filter: HistoryFilter): Boolean {
        if (filter.storeQuery.isNotBlank() && !storeName.contains(filter.storeQuery, ignoreCase = true)) return false
        if (filter.category != null && category != filter.category) return false
        if (filter.dateFromMs != null && timestampMs < filter.dateFromMs) return false
        if (filter.dateToMs != null && timestampMs > filter.dateToMs) return false
        if (filter.minAmount != null && totalAmount < filter.minAmount) return false
        if (filter.maxAmount != null && totalAmount > filter.maxAmount) return false
        return true
    }

    fun setStoreQuery(query: String) { _filter.value = _filter.value.copy(storeQuery = query) }
    fun setCategory(category: String?) { _filter.value = _filter.value.copy(category = category) }
    fun setDateRange(fromMs: Long?, toMs: Long?) { _filter.value = _filter.value.copy(dateFromMs = fromMs, dateToMs = toMs) }
    fun setAmountRange(min: Double?, max: Double?) { _filter.value = _filter.value.copy(minAmount = min, maxAmount = max) }
    fun clearFilters() { _filter.value = HistoryFilter() }

    fun buildExportText(): String {
        val purchases = _uiState.value.filteredPurchases
        val header = "Fecha;Hora;Tienda;Categoria;Monto;Productos"
        val rows = purchases.joinToString("\n") { p ->
            val products = p.products.joinToString(" | ") { it.name }
            "${p.date};${p.time};${p.storeName};${p.category};${
                String.format(Locale.US, "%.2f", p.totalAmount)
            };$products"
        }
        return if (rows.isBlank()) header else "$header\n$rows"
    }
}
