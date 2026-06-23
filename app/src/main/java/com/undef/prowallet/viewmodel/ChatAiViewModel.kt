package com.undef.prowallet.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.undef.prowallet.R
import com.undef.prowallet.data.AppRepository
import com.undef.prowallet.domain.Purchase
import com.undef.prowallet.util.SessionManager
import com.undef.prowallet.util.isCurrentMonth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Locale

data class ChatMessage(
    val textRes: Int? = null,
    val textArgs: List<Any> = emptyList(),
    val rawText: String? = null,
    val isUser: Boolean = false,
    val insightPercent: Int? = null
)

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList()
)

/**
 * Asistente local basado en reglas: no usa un LLM, pero todas las respuestas
 * se calculan en el momento a partir de Room (compras) y DataStore (presupuesto).
 */
class ChatAiViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppRepository(application)
    private val sessionManager = SessionManager(application)

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var latestPurchases: List<Purchase> = emptyList()
    private var latestBudget: Double = 0.0
    private var initialized = false

    init {
        viewModelScope.launch {
            combine(repository.purchasesFlow, sessionManager.monthlyBudget) { purchases, budget ->
                purchases to budget
            }.collect { (purchases, budget) ->
                latestPurchases = purchases
                latestBudget = budget
                if (!initialized) {
                    initialized = true
                    _uiState.value = ChatUiState(messages = listOf(greetingMessage()))
                }
            }
        }
    }

    fun sendMessage(text: String) {
        val trimmed = text.trim()
        if (trimmed.isBlank()) return
        val userMessage = ChatMessage(rawText = trimmed, isUser = true)
        val reply = buildReply(trimmed)
        _uiState.value = _uiState.value.copy(messages = _uiState.value.messages + userMessage + reply)
    }

    private fun greetingMessage(): ChatMessage = ChatMessage(textRes = R.string.proassistant_greeting, isUser = false)

    private fun buildReply(input: String): ChatMessage {
        val normalized = input.lowercase(Locale.getDefault())
        return when {
            "presupuesto" in normalized || "budget" in normalized -> budgetReply()
            "semana" in normalized || "week" in normalized -> weeklyReply()
            "categor" in normalized -> categoryReply()
            else -> fallbackReply()
        }
    }

    private fun budgetReply(): ChatMessage {
        val spent = latestPurchases.filter { it.isCurrentMonth() }.sumOf { it.totalAmount }
        if (latestBudget <= 0) {
            return ChatMessage(textRes = R.string.chat_no_budget_set, isUser = false)
        }
        val percent = ((spent / latestBudget) * 100).toInt().coerceAtLeast(0)
        val remaining = (latestBudget - spent)
        return ChatMessage(
            textRes = R.string.chat_budget_status,
            textArgs = listOf("%.0f".format(spent), "%.0f".format(latestBudget), percent, "%.0f".format(remaining)),
            isUser = false,
            insightPercent = percent
        )
    }

    private fun weeklyReply(): ChatMessage {
        val now = System.currentTimeMillis()
        val sevenDaysMs = 7L * 24 * 60 * 60 * 1000
        val weekPurchases = latestPurchases.filter { it.timestampMs > 0 && now - it.timestampMs <= sevenDaysMs }
        if (weekPurchases.isEmpty()) {
            return ChatMessage(textRes = R.string.chat_no_purchases_week, isUser = false)
        }
        val total = weekPurchases.sumOf { it.totalAmount }
        return ChatMessage(
            textRes = R.string.chat_week_summary,
            textArgs = listOf(weekPurchases.size, "%.0f".format(total)),
            isUser = false
        )
    }

    private fun categoryReply(): ChatMessage {
        val byCategory = latestPurchases
            .filter { it.isCurrentMonth() }
            .groupBy { it.category }
            .mapValues { (_, list) -> list.sumOf { it.totalAmount } }
            .toList()
            .sortedByDescending { it.second }
            .take(3)
        if (byCategory.isEmpty()) {
            return ChatMessage(textRes = R.string.chat_no_purchases_month, isUser = false)
        }
        val summary = byCategory.joinToString(", ") { (category, total) -> "$category ($${"%.0f".format(total)})" }
        return ChatMessage(textRes = R.string.chat_top_categories, textArgs = listOf(summary), isUser = false)
    }

    private fun fallbackReply(): ChatMessage {
        val spent = latestPurchases.filter { it.isCurrentMonth() }.sumOf { it.totalAmount }
        return ChatMessage(
            textRes = R.string.chat_fallback,
            textArgs = listOf("%.0f".format(spent)),
            isUser = false
        )
    }
}
