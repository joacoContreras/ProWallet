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
import java.text.DateFormatSymbols
import java.util.Calendar
import java.util.Locale

enum class InflationPeriod { MONTHLY, QUARTERLY }

data class ProductPriceChange(
    val productName: String,
    val category: String,
    val previousPrice: Double,
    val currentPrice: Double
) {
    val changePercent: Double
        get() = if (previousPrice > 0) ((currentPrice - previousPrice) / previousPrice) * 100 else 0.0
}

data class PersonalInflationUiState(
    val period: InflationPeriod = InflationPeriod.MONTHLY,
    val hasEnoughData: Boolean = false,
    val personalInflationRate: Double = 0.0,
    val monthlyTrend: List<Pair<String, Float>> = emptyList(),
    val priceChanges: List<ProductPriceChange> = emptyList(),
    val insightText: String? = null
)

/**
 * Inflación personal real: compara el precio pagado por los mismos productos
 * (por código) entre dos ventanas de tiempo, usando PurchasedItemEntity.price
 * (precio pagado en cada compra). Si el usuario no recompró ningún producto
 * en ambas ventanas, no hay forma honesta de calcular una tasa: se reporta
 * "sin datos suficientes" en vez de inventar un número.
 */
class PersonalInflationViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppRepository(application)

    private val _uiState = MutableStateFlow(PersonalInflationUiState())
    val uiState: StateFlow<PersonalInflationUiState> = _uiState.asStateFlow()

    private var latestPurchases: List<Purchase> = emptyList()

    init {
        viewModelScope.launch {
            repository.purchasesFlow.collect { purchases ->
                latestPurchases = purchases
                recompute()
            }
        }
    }

    fun setPeriod(period: InflationPeriod) {
        _uiState.value = _uiState.value.copy(period = period)
        recompute()
    }

    private fun recompute() {
        val period = _uiState.value.period
        val monthsPerWindow = if (period == InflationPeriod.MONTHLY) 1 else 3

        val currentWindowProducts = pricesInWindow(latestPurchases, monthsAgoStart = 0, monthsAgoEnd = monthsPerWindow - 1)
        val previousWindowProducts = pricesInWindow(latestPurchases, monthsAgoStart = monthsPerWindow, monthsAgoEnd = monthsPerWindow * 2 - 1)

        val overlapping = currentWindowProducts.keys.intersect(previousWindowProducts.keys)
        val priceChanges = overlapping.map { code ->
            val (name, category, currentPrice) = currentWindowProducts[code]!!
            val (_, _, previousPrice) = previousWindowProducts[code]!!
            ProductPriceChange(name, category, previousPrice, currentPrice)
        }.sortedByDescending { it.changePercent }

        val inflationRate = if (priceChanges.isNotEmpty()) priceChanges.map { it.changePercent }.average() else 0.0

        // Tendencia: inflación personal mes a mes (cada mes vs. el mes inmediatamente anterior),
        // siempre con ventanas de 1 mes para que las 6 barras sean comparables entre sí.
        val monthlyTrend = (5 downTo 0).map { monthsAgo ->
            val cal = Calendar.getInstance().also { it.add(Calendar.MONTH, -monthsAgo) }
            val label = DateFormatSymbols.getInstance(Locale.getDefault()).shortMonths[cal.get(Calendar.MONTH)]
            val monthProducts = pricesInWindow(latestPurchases, monthsAgoStart = monthsAgo, monthsAgoEnd = monthsAgo)
            val priorMonthProducts = pricesInWindow(latestPurchases, monthsAgoStart = monthsAgo + 1, monthsAgoEnd = monthsAgo + 1)
            val sharedCodes = monthProducts.keys.intersect(priorMonthProducts.keys)
            val rate = if (sharedCodes.isEmpty()) 0f else sharedCodes.map { code ->
                val current = monthProducts[code]!!.third
                val previous = priorMonthProducts[code]!!.third
                if (previous > 0) (((current - previous) / previous) * 100).toFloat() else 0f
            }.average().toFloat()
            Pair(label, rate)
        }

        val insight = priceChanges.firstOrNull { it.changePercent > 0 }?.let {
            "${it.productName} subió un ${"%.0f".format(it.changePercent)}% desde la última vez que lo compraste."
        }

        _uiState.value = _uiState.value.copy(
            hasEnoughData = priceChanges.isNotEmpty(),
            personalInflationRate = inflationRate,
            monthlyTrend = monthlyTrend,
            priceChanges = priceChanges,
            insightText = insight
        )
    }

    /** code -> (productName, category, average price paid) para compras dentro de la ventana (monthsAgoStart..monthsAgoEnd). */
    private fun pricesInWindow(
        purchases: List<Purchase>,
        monthsAgoStart: Int,
        monthsAgoEnd: Int
    ): Map<String, Triple<String, String, Double>> {
        val now = Calendar.getInstance()
        val windowStart = (now.clone() as Calendar).apply { add(Calendar.MONTH, -monthsAgoEnd); set(Calendar.DAY_OF_MONTH, 1) }
        val windowEnd = (now.clone() as Calendar).apply { add(Calendar.MONTH, -monthsAgoStart + 1); set(Calendar.DAY_OF_MONTH, 1) }

        data class Entry(val name: String, val category: String, val price: Double)
        val entriesByCode = mutableMapOf<String, MutableList<Entry>>()

        purchases.forEach { purchase ->
            if (purchase.timestampMs <= 0) return@forEach
            val inWindow = purchase.timestampMs >= windowStart.timeInMillis && purchase.timestampMs < windowEnd.timeInMillis
            if (!inWindow) return@forEach
            purchase.products.forEach { product ->
                entriesByCode.getOrPut(product.code) { mutableListOf() }
                    .add(Entry(product.name, purchase.category, product.price))
            }
        }

        return entriesByCode.mapValues { (_, entries) ->
            Triple(entries.last().name, entries.last().category, entries.map { it.price }.average())
        }
    }
}
