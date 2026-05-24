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

data class PurchaseDetailUiState(
    val isLoading: Boolean = true,
    val purchase: Purchase? = null
)

class PurchaseDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppRepository(application)

    private val _uiState = MutableStateFlow(PurchaseDetailUiState())
    val uiState: StateFlow<PurchaseDetailUiState> = _uiState.asStateFlow()

    fun loadPurchase(id: String) {
        _uiState.value = PurchaseDetailUiState(isLoading = true)
        viewModelScope.launch {
            val numericId = id.toIntOrNull()
            val purchase = if (numericId != null) repository.getPurchaseById(numericId) else null
            _uiState.value = PurchaseDetailUiState(isLoading = false, purchase = purchase)
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
