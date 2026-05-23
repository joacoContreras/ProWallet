package com.undef.prowallet.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.undef.prowallet.data.PurchaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TopStoresUiState(
    val totalSpentMonth: Double = 0.0
)

class TopStoresViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(TopStoresUiState())
    val uiState: StateFlow<TopStoresUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            PurchaseRepository.purchases.collect { purchases ->
                _uiState.value = TopStoresUiState(
                    totalSpentMonth = purchases.sumOf { it.totalAmount }
                )
            }
        }
    }
}
