package com.undef.prowallet.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.undef.prowallet.R
import com.undef.prowallet.data.AppRepository
import com.undef.prowallet.util.SessionManager
import com.undef.prowallet.util.isCurrentMonth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

enum class NotificationKind { BUDGET, CATEGORY, PURCHASE, SAVINGS }

data class AppNotification(
    val kind: NotificationKind,
    val textRes: Int,
    val textArgs: List<Any> = emptyList()
)

data class NotificationsUiState(
    val notifications: List<AppNotification> = emptyList()
)

/**
 * Todas las notificaciones se recalculan a partir de Room/DataStore en cada
 * emisión: no existe una tabla de notificaciones, por lo que no hay concepto
 * real de "leído" ni de marca de tiempo de entrega.
 */
class NotificationsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppRepository(application)
    private val sessionManager = SessionManager(application)

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.purchasesFlow,
                sessionManager.monthlyBudget,
                sessionManager.savingsPercentage
            ) { purchases, budget, savingsPercentage ->
                Triple(purchases, budget, savingsPercentage)
            }.collect { (purchases, budget, savingsPercentage) ->
                _uiState.value = NotificationsUiState(
                    notifications = buildNotifications(purchases, budget, savingsPercentage)
                )
            }
        }
    }

    private fun buildNotifications(
        purchases: List<com.undef.prowallet.domain.Purchase>,
        budget: Double,
        savingsPercentage: Float
    ): List<AppNotification> {
        val current = purchases.filter { it.isCurrentMonth() }
        val result = mutableListOf<AppNotification>()

        val spent = current.sumOf { it.totalAmount }
        if (budget > 0) {
            val percent = ((spent / budget) * 100).toInt()
            when {
                spent > budget -> result.add(
                    AppNotification(
                        NotificationKind.BUDGET,
                        R.string.notif_budget_over_format,
                        listOf("%.0f".format(spent - budget), percent)
                    )
                )
                percent >= 80 -> result.add(
                    AppNotification(
                        NotificationKind.BUDGET,
                        R.string.notif_budget_near_format,
                        listOf(percent, "%.0f".format(spent), "%.0f".format(budget))
                    )
                )
            }
        }

        val topCategory = current
            .groupBy { it.category }
            .mapValues { (_, list) -> list.sumOf { it.totalAmount } }
            .maxByOrNull { it.value }
        if (topCategory != null) {
            result.add(
                AppNotification(
                    NotificationKind.CATEGORY,
                    R.string.notif_top_category_format,
                    listOf(topCategory.key, "%.0f".format(topCategory.value))
                )
            )
        }

        val biggestPurchase = current.maxByOrNull { it.totalAmount }
        if (biggestPurchase != null) {
            result.add(
                AppNotification(
                    NotificationKind.PURCHASE,
                    R.string.notif_biggest_purchase_format,
                    listOf(biggestPurchase.storeName, "%.0f".format(biggestPurchase.totalAmount))
                )
            )
        }

        if (savingsPercentage > 0) {
            result.add(
                AppNotification(
                    NotificationKind.SAVINGS,
                    R.string.notif_auto_savings_format,
                    listOf(savingsPercentage.toInt())
                )
            )
        }

        return result
    }
}
