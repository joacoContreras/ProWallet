package com.undef.prowallet.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.undef.prowallet.data.AppRepository
import com.undef.prowallet.data.remote.PreciosClarosProductDto
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

private val SPANISH_STOP_WORDS = setOf(
    "con", "del", "los", "las", "una", "uno", "por", "sin",
    "sobre", "para", "como", "cada", "pero", "mas", "sus", "que"
)
private const val MIN_MATCH_RATIO = 0.50
private const val WEIGHT_MATCH    = 0.70
private const val WEIGHT_SUC      = 0.15
private const val BONUS_FIRST     = 0.15

private fun String.toQueryWords(): List<String> =
    trim().lowercase(Locale.ROOT)
        .split("\\s+".toRegex())
        .filter { it.length >= 3 && it !in SPANISH_STOP_WORDS }

private fun scoreDto(
    dto: PreciosClarosProductDto,
    queryWords: List<String>,
    firstQueryWord: String,
    maxSucursales: Int
): Double {
    val dtoWords = dto.nombre.lowercase(Locale.ROOT).split("\\s+".toRegex())
    val matched = queryWords.count { qw -> dtoWords.any { dw -> dw.startsWith(qw) } }
    val matchRatio = matched.toDouble() / queryWords.size
    if (matchRatio < MIN_MATCH_RATIO) return -1.0
    val normSuc = if (maxSucursales > 0) dto.sucursalesDisponibles.toDouble() / maxSucursales else 0.0
    val firstBonus = if ((dtoWords.firstOrNull() ?: "").startsWith(firstQueryWord)) BONUS_FIRST else 0.0
    return (matchRatio * WEIGHT_MATCH) + (normSuc * WEIGHT_SUC) + firstBonus
}

data class PurchaseDetailUiState(
    val isLoading: Boolean = true,
    val purchase: Purchase? = null,
    val apiPriceMap: Map<String, ApiPriceResult> = emptyMap(),
    val isLoadingPrices: Boolean = false
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

            _uiState.value = _uiState.value.copy(isLoadingPrices = true)

            val (lat, lng) = coords

            val deferreds = purchase.products.map { product ->
                async { product.name to repository.searchProductPrices(lat, lng, product.name) }
            }

            val apiPriceMap = deferreds.awaitAll()
                .mapNotNull { (name, results) ->
                    val queryWords = name.toQueryWords()
                    if (queryWords.isEmpty()) return@mapNotNull null

                    val firstQueryWord = queryWords.first()
                    val maxSucursales = results.maxOfOrNull { it.sucursalesDisponibles } ?: 0

                    val best = results
                        .filter { it.precioMin > 0.0 }
                        .map { dto -> dto to scoreDto(dto, queryWords, firstQueryWord, maxSucursales) }
                        .filter { (_, score) -> score >= 0.0 }
                        .maxByOrNull { (_, score) -> score }
                        ?.first
                        ?: return@mapNotNull null

                    name.trim().lowercase(Locale.ROOT) to ApiPriceResult(best.precioMin, best.precioMax)
                }
                .toMap()

            _uiState.value = _uiState.value.copy(apiPriceMap = apiPriceMap, isLoadingPrices = false)
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
