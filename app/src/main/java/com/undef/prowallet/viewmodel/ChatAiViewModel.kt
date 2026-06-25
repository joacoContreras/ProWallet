package com.undef.prowallet.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.undef.prowallet.R
import com.undef.prowallet.data.AppRepository
import com.undef.prowallet.data.ocr.GroqApiService
import com.undef.prowallet.data.ocr.GroqChatRequest
import com.undef.prowallet.data.ocr.GroqMessage
import com.undef.prowallet.data.ocr.GroqResponseFormat
import com.undef.prowallet.domain.Purchase
import com.undef.prowallet.util.SessionManager
import com.undef.prowallet.util.isCurrentMonth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Calendar
import java.util.Locale

data class ChatMessage(
    val textRes: Int? = null,
    val textArgs: List<Any> = emptyList(),
    val rawText: String? = null,
    val isUser: Boolean = false,
    val insightPercent: Int? = null,
    val isThinking: Boolean = false
)

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val options: List<Int> = emptyList()
)

/**
 * Asistente financiero: usa Groq cuando hay API Key configurada; en caso contrario
 * aplica un motor local que lee datos reales de Room y DataStore.
 */
class ChatAiViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppRepository(application)
    private val sessionManager = SessionManager(application)

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var latestPurchases: List<Purchase> = emptyList()
    private var latestBudget: Double = 0.0
    private var latestIncome: Double = 0.0
    private var latestGroqKey: String = ""
    private var initialized = false

    init {
        viewModelScope.launch {
            combine(
                repository.purchasesFlow,
                sessionManager.monthlyBudget,
                sessionManager.monthlyIncome,
                sessionManager.groqApiKey
            ) { purchases, budget, income, groqKey ->
                Triple(purchases, Pair(budget, income), groqKey)
            }.collect { (purchases, budgetIncome, groqKey) ->
                latestPurchases = purchases
                latestBudget = budgetIncome.first
                latestIncome = budgetIncome.second
                latestGroqKey = groqKey
                if (!initialized) {
                    initialized = true
                    _uiState.value = ChatUiState(
                        messages = listOf(greetingMessage()),
                        options = mainMenuOptions()
                    )
                }
            }
        }
    }

    fun sendMessage(text: String) {
        val trimmed = text.trim()
        if (trimmed.isBlank()) return
        val userMessage = ChatMessage(rawText = trimmed, isUser = true)
        _uiState.value = _uiState.value.copy(
            messages = _uiState.value.messages + userMessage,
            options = emptyList()
        )
        if (latestGroqKey.isNotBlank()) {
            sendToGroq(trimmed)
        } else {
            val reply = buildLocalReply(trimmed)
            _uiState.value = _uiState.value.copy(
                messages = _uiState.value.messages + reply,
                options = mainMenuOptions()
            )
        }
    }

    fun selectOption(optionRes: Int) {
        val optionText = getApplication<Application>().getString(optionRes)
        sendMessage(optionText)
    }

    // ── Groq path ─────────────────────────────────────────────────────────────

    private fun sendToGroq(userText: String) {
        val thinkingMsg = ChatMessage(textRes = R.string.chat_thinking, isThinking = true)
        _uiState.value = _uiState.value.copy(messages = _uiState.value.messages + thinkingMsg)

        viewModelScope.launch {
            try {
                val context = buildFinancialContext()
                val systemPrompt = """
                    Sos ProAsistente, el asistente financiero personal de la app ProWallet.
                    Respondé en español, de forma concisa y amigable (máximo 3 oraciones).
                    Solo respondé preguntas relacionadas con las finanzas personales.
                    Datos financieros actuales del usuario:
                    $context
                    Respondé con texto plano, sin markdown ni asteriscos.
                """.trimIndent()

                val request = GroqChatRequest(
                    model = "llama-3.3-70b-versatile",
                    messages = listOf(
                        GroqMessage("system", systemPrompt),
                        GroqMessage("user", userText)
                    ),
                    responseFormat = GroqResponseFormat("text")
                )

                val service = Retrofit.Builder()
                    .baseUrl("https://api.groq.com/openai/v1/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(GroqApiService::class.java)

                val response = service.getChatCompletion("Bearer $latestGroqKey", request)
                val replyText = response.choices.firstOrNull()?.message?.content
                    ?: getApplication<Application>().getString(R.string.chat_groq_error)

                replaceThinking(ChatMessage(rawText = replyText))
            } catch (_: Exception) {
                replaceThinking(ChatMessage(textRes = R.string.chat_groq_error))
            }
            _uiState.value = _uiState.value.copy(options = mainMenuOptions())
        }
    }

    private fun replaceThinking(reply: ChatMessage) {
        val messages = _uiState.value.messages.toMutableList()
        val idx = messages.indexOfLast { it.isThinking }
        if (idx >= 0) messages[idx] = reply else messages.add(reply)
        _uiState.value = _uiState.value.copy(messages = messages)
    }

    private fun buildFinancialContext(): String {
        val monthPurchases = latestPurchases.filter { it.isCurrentMonth() }
        val monthSpend = monthPurchases.sumOf { it.totalAmount }
        val topCategories = monthPurchases
            .groupBy { it.category }
            .mapValues { (_, list) -> list.sumOf { it.totalAmount } }
            .toList().sortedByDescending { it.second }.take(3)
            .joinToString(", ") { (cat, amt) -> "$cat: ${"%.0f".format(amt)}" }
        val mostExpensive = latestPurchases.maxByOrNull { it.totalAmount }
        val prevSpend = previousMonthSpend()
        return buildString {
            appendLine("- Presupuesto mensual: ${"%.0f".format(latestBudget)}")
            appendLine("- Ingreso mensual: ${"%.0f".format(latestIncome)}")
            appendLine("- Gasto este mes: ${"%.0f".format(monthSpend)} (${monthPurchases.size} compras)")
            if (topCategories.isNotEmpty()) appendLine("- Top categorías: $topCategories")
            if (mostExpensive != null) appendLine("- Compra más cara: ${mostExpensive.storeName} \$${"%,.0f".format(mostExpensive.totalAmount)}")
            if (prevSpend > 0) appendLine("- Gasto mes anterior: ${"%.0f".format(prevSpend)}")
        }
    }

    // ── Motor local expandido ─────────────────────────────────────────────────

    private fun buildLocalReply(input: String): ChatMessage {
        val n = input.lowercase(Locale.getDefault())
        return when {
            containsAny(n, "más cara", "mas cara", "mayor", "costosa", "expensive", "más grande", "mas grande") ->
                mostExpensivePurchaseReply()
            containsAny(n, "compar", "mes pasado", "mes anterior", "versus", " vs ") ->
                monthComparisonReply()
            containsAny(n, "ahorrar", "recomiend", "debería", "deberia", "consejo", "tip", "sugier") ->
                savingsAdviceReply()
            containsAny(n, "cuánto gasté en", "cuanto gaste en", "gaste en", "gasté en") ->
                storeQueryReply(input)
            containsAny(n, "presupuesto", "budget", "verificar presupuesto") ->
                budgetReply()
            containsAny(n, "semana", "week", "últimos días", "ultimos dias", "analizar") ->
                weeklyReply()
            containsAny(n, "categor", "categorías principales") ->
                categoryReply()
            else -> fallbackReply()
        }
    }

    private fun containsAny(text: String, vararg keywords: String) =
        keywords.any { text.contains(it) }

    private fun mainMenuOptions(): List<Int> = listOf(
        R.string.chat_option_expenses,
        R.string.chat_option_budget,
        R.string.chat_option_categories,
        R.string.chat_option_help
    )

    private fun greetingMessage() = ChatMessage(textRes = R.string.proassistant_greeting)

    // ── Respuestas del motor local ────────────────────────────────────────────

    private fun budgetReply(): ChatMessage {
        val spent = latestPurchases.filter { it.isCurrentMonth() }.sumOf { it.totalAmount }
        if (latestBudget <= 0) return ChatMessage(textRes = R.string.chat_no_budget_set)
        val percent = ((spent / latestBudget) * 100).toInt().coerceAtLeast(0)
        val remaining = latestBudget - spent
        return ChatMessage(
            textRes = R.string.chat_budget_status,
            textArgs = listOf("%.0f".format(spent), "%.0f".format(latestBudget), percent, "%.0f".format(remaining)),
            insightPercent = percent
        )
    }

    private fun weeklyReply(): ChatMessage {
        val now = System.currentTimeMillis()
        val sevenDaysMs = 7L * 24 * 60 * 60 * 1000
        val weekPurchases = latestPurchases.filter { it.timestampMs > 0 && now - it.timestampMs <= sevenDaysMs }
        if (weekPurchases.isEmpty()) return ChatMessage(textRes = R.string.chat_no_purchases_week)
        val total = weekPurchases.sumOf { it.totalAmount }
        return ChatMessage(
            textRes = R.string.chat_week_summary,
            textArgs = listOf(weekPurchases.size, "%.0f".format(total))
        )
    }

    private fun categoryReply(): ChatMessage {
        val byCategory = latestPurchases
            .filter { it.isCurrentMonth() }
            .groupBy { it.category }
            .mapValues { (_, list) -> list.sumOf { it.totalAmount } }
            .toList().sortedByDescending { it.second }.take(3)
        if (byCategory.isEmpty()) return ChatMessage(textRes = R.string.chat_no_purchases_month)
        val summary = byCategory.joinToString(", ") { (cat, total) -> "$cat ($${"%.0f".format(total)})" }
        return ChatMessage(textRes = R.string.chat_top_categories, textArgs = listOf(summary))
    }

    private fun mostExpensivePurchaseReply(): ChatMessage {
        if (latestPurchases.isEmpty()) return ChatMessage(textRes = R.string.chat_no_purchases)
        val purchase = latestPurchases.maxByOrNull { it.totalAmount }
            ?: return ChatMessage(textRes = R.string.chat_no_purchases)
        return ChatMessage(
            textRes = R.string.chat_most_expensive_format,
            textArgs = listOf(purchase.storeName, "%.0f".format(purchase.totalAmount), purchase.date)
        )
    }

    private fun monthComparisonReply(): ChatMessage {
        val currentSpend = latestPurchases.filter { it.isCurrentMonth() }.sumOf { it.totalAmount }
        val prevSpend = previousMonthSpend()
        if (prevSpend <= 0) {
            return ChatMessage(
                textRes = R.string.chat_comparison_no_prev,
                textArgs = listOf("%.0f".format(currentSpend))
            )
        }
        val app = getApplication<Application>()
        val diff = kotlin.math.abs(currentSpend - prevSpend)
        val direction = if (currentSpend >= prevSpend)
            app.getString(R.string.chat_comparison_more)
        else
            app.getString(R.string.chat_comparison_less)
        return ChatMessage(
            textRes = R.string.chat_comparison_format,
            textArgs = listOf("%.0f".format(currentSpend), "%.0f".format(prevSpend), "${"%.0f".format(diff)} ($direction)")
        )
    }

    private fun savingsAdviceReply(): ChatMessage {
        val byCategory = latestPurchases
            .filter { it.isCurrentMonth() }
            .groupBy { it.category }
            .mapValues { (_, list) -> list.sumOf { it.totalAmount } }
            .toList().sortedByDescending { it.second }
        if (byCategory.isEmpty()) return ChatMessage(textRes = R.string.chat_savings_no_data)
        val (topCategory, topAmount) = byCategory.first()
        val potential = topAmount * 0.20
        return ChatMessage(
            textRes = R.string.chat_savings_advice_format,
            textArgs = listOf(topCategory, "%.0f".format(topAmount), "%.0f".format(potential))
        )
    }

    private fun storeQueryReply(originalInput: String): ChatMessage {
        val n = originalInput.lowercase(Locale.getDefault())
        val storeKeyword = when {
            "gasté en " in n -> n.substringAfter("gasté en ").trim().split(" ").firstOrNull() ?: ""
            "gaste en " in n -> n.substringAfter("gaste en ").trim().split(" ").firstOrNull() ?: ""
            "en " in n -> n.substringAfterLast("en ").trim().split(" ").firstOrNull() ?: ""
            else -> n.split(" ").lastOrNull { it.length > 3 } ?: ""
        }
        if (storeKeyword.isBlank()) return fallbackReply()
        val matching = latestPurchases.filter { it.storeName.contains(storeKeyword, ignoreCase = true) }
        if (matching.isEmpty()) {
            return ChatMessage(textRes = R.string.chat_store_not_found_format, textArgs = listOf(storeKeyword))
        }
        val total = matching.sumOf { it.totalAmount }
        return ChatMessage(
            textRes = R.string.chat_store_query_format,
            textArgs = listOf("%.0f".format(total), storeKeyword, matching.size)
        )
    }

    private fun fallbackReply(): ChatMessage {
        val spent = latestPurchases.filter { it.isCurrentMonth() }.sumOf { it.totalAmount }
        return ChatMessage(textRes = R.string.chat_fallback, textArgs = listOf("%.0f".format(spent)))
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private fun previousMonthSpend(): Double {
        val cal = Calendar.getInstance().also { it.add(Calendar.MONTH, -1) }
        val prevYear = cal.get(Calendar.YEAR)
        val prevMonth = cal.get(Calendar.MONTH)
        return latestPurchases.filter { p ->
            if (p.timestampMs <= 0) return@filter false
            val pCal = Calendar.getInstance().also { it.timeInMillis = p.timestampMs }
            pCal.get(Calendar.YEAR) == prevYear && pCal.get(Calendar.MONTH) == prevMonth
        }.sumOf { it.totalAmount }
    }
}
