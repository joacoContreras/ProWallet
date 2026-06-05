package com.undef.prowallet.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.undef.prowallet.data.AppRepository
import com.undef.prowallet.domain.Purchase
import com.undef.prowallet.util.LocationHelper
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

data class ApiPriceResult(val precioMin: Double, val precioMax: Double)

data class PurchaseDetailUiState(
    val isLoading: Boolean = true,
    val purchase: Purchase? = null,
    val apiPriceMap: Map<String, ApiPriceResult> = emptyMap()
)

class PurchaseDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppRepository(application)

    private val _uiState = MutableStateFlow(PurchaseDetailUiState())
    val uiState: StateFlow<PurchaseDetailUiState> = _uiState.asStateFlow()

    fun loadPurchase(id: String) {
        val numericId = id.toIntOrNull() ?: run {
            _uiState.value = PurchaseDetailUiState(isLoading = false)
            return
        }
        _uiState.value = PurchaseDetailUiState(isLoading = true)
        viewModelScope.launch {
            val purchase = repository.getPurchaseById(numericId)
            _uiState.value = _uiState.value.copy(isLoading = false, purchase = purchase)

            if (purchase == null) return@launch

            val coords = LocationHelper(getApplication()).getLocation()
            if (coords == null) return@launch

            val (lat, lng) = coords

            val deferreds = purchase.products.map { product ->
                async { product.name to repository.searchProductPrices(lat, lng, product.name) }
            }

            val apiPriceMap = deferreds.awaitAll()
                .mapNotNull { (name, results) ->
                    val first = results.firstOrNull() ?: return@mapNotNull null
                    name.trim().lowercase(Locale.ROOT) to ApiPriceResult(first.precioMin, first.precioMax)
                }
                .toMap()

            _uiState.value = _uiState.value.copy(apiPriceMap = apiPriceMap)
        }
    }

    fun deletePurchase(id: String, onDone: () -> Unit) {
        val numericId = id.toIntOrNull() ?: return
        viewModelScope.launch {
            repository.deletePurchase(numericId)
            onDone()
        }
    }
}
