package com.undef.prowallet.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.undef.prowallet.data.PurchaseRepository
import com.undef.prowallet.domain.Purchase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HistoryUiState(
    val allPurchases: List<Purchase> = emptyList()
)

class HistoryViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            PurchaseRepository.purchases.collect { purchases ->
                _uiState.value = HistoryUiState(allPurchases = purchases)
            }
        }
    }
}
