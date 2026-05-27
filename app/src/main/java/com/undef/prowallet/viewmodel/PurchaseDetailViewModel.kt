package com.undef.prowallet.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.undef.prowallet.data.AppRepository
import com.undef.prowallet.domain.Purchase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

data class PurchaseDetailUiState(
    val isLoading: Boolean = true,
    val purchase: Purchase? = null,
    val apiPriceMap: Map<String, Double> = emptyMap()
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
        }
        viewModelScope.launch {
            val apiMap = try {
                repository.getApiProducts()
                    .associateBy { it.nombre.trim().lowercase(Locale.ROOT) }
                    .mapValues { it.value.precioPromedio }
            } catch (e: Exception) { emptyMap() }
            _uiState.value = _uiState.value.copy(apiPriceMap = apiMap)
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
