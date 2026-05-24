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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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
                val now = Calendar.getInstance()
                val spent = purchases
                    .filter { it.isCurrentMonth(now) }
                    .sumOf { it.totalAmount }
                _uiState.value = TopStoresUiState(totalSpentMonth = spent)
            }
        }
    }

    private fun Purchase.isCurrentMonth(now: Calendar): Boolean {
        return try {
            val cal = Calendar.getInstance()
            cal.time = SimpleDateFormat("MM/dd/yy", Locale.getDefault()).parse(date)!!
            cal.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                cal.get(Calendar.MONTH) == now.get(Calendar.MONTH)
        } catch (e: Exception) { false }
    }
}
