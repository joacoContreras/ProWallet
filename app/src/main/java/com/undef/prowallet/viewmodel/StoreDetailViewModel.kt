package com.undef.prowallet.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.undef.prowallet.data.AppRepository
import com.undef.prowallet.util.isCurrentMonth
import com.undef.prowallet.util.isInMonth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

data class StoreDetailUiState(
    val totalThisMonth: Double = 0.0,
    val totalLastMonth: Double = 0.0,
    val categoryDistribution: List<Pair<String, Double>> = emptyList(),
    val isLoaded: Boolean = false
)

class StoreDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppRepository(application)

    private val _uiState = MutableStateFlow(StoreDetailUiState())
    val uiState: StateFlow<StoreDetailUiState> = _uiState.asStateFlow()

    fun loadStore(storeName: String) {
        viewModelScope.launch {
            repository.purchasesFlow.collect { allPurchases ->
                val storePurchases = allPurchases.filter { it.storeName == storeName }

                val thisMonthPurchases = storePurchases.filter { it.isCurrentMonth() }

                val lastMonthCal = Calendar.getInstance().apply { add(Calendar.MONTH, -1) }
                val lastMonthYear = lastMonthCal.get(Calendar.YEAR)
                val lastMonthMonth = lastMonthCal.get(Calendar.MONTH)
                val lastMonthPurchases = storePurchases.filter { it.isInMonth(lastMonthYear, lastMonthMonth) }

                val totalThis = thisMonthPurchases.sumOf { it.totalAmount }
                val totalLast = lastMonthPurchases.sumOf { it.totalAmount }

                val catDist = thisMonthPurchases
                    .groupBy { it.category }
                    .mapValues { (_, list) -> list.sumOf { it.totalAmount } }
                    .toList()
                    .sortedByDescending { it.second }

                _uiState.value = StoreDetailUiState(
                    totalThisMonth = totalThis,
                    totalLastMonth = totalLast,
                    categoryDistribution = catDist,
                    isLoaded = true
                )
            }
        }
    }
}
