package com.undef.prowallet.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.undef.prowallet.data.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TopStoresUiState(
    val totalSpentMonth: Double = 0.0
)

class TopStoresViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppRepository(application)

    private val _uiState = MutableStateFlow(TopStoresUiState())
    val uiState: StateFlow<TopStoresUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.purchasesFlow.collect { purchases ->
                _uiState.value = TopStoresUiState(totalSpentMonth = purchases.sumOf { it.totalAmount })
            }
        }
    }
}
