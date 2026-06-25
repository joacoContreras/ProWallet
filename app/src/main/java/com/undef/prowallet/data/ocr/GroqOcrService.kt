package com.undef.prowallet.data.ocr

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface GroqApiService {
    @POST("chat/completions")
    suspend fun getChatCompletion(
        @Header("Authorization") authHeader: String,
        @Body request: GroqChatRequest
    ): GroqChatResponse
}

data class GroqMessage(val role: String, val content: String)

data class GroqResponseFormat(val type: String)

data class GroqChatRequest(
    val model: String,
    @SerializedName("messages") val messages: List<GroqMessage>,
    @SerializedName("response_format") val responseFormat: GroqResponseFormat
)

data class GroqChoice(val message: GroqMessage)

data class GroqChatResponse(val choices: List<GroqChoice>)

object GroqOcrService {

    private val apiService: GroqApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.groq.com/openai/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GroqApiService::class.java)
    }

    suspend fun parseWithGroq(rawText: String, apiKey: String): ParsedTicket {
        val systemPrompt = """
            You are an expert receipt parser. Analyze the raw text extracted from a purchase ticket (OCR) and return a JSON object with the following structure:
            {
              "storeName": "Name of the business/store (string or null)",
              "date": "Purchase date in MM/dd/yy format (string or null)",
              "time": "Purchase time in HH:mm format (string or null)",
              "items": [
                {
                  "name": "Clean name/description of the product, removing barcodes and quantity details (string)",
                  "price": 123.45 (number)
                }
              ],
              "total": 123.45 (number or null)
            }
            Ensure:
            1. The date is the actual purchase date, NOT the business registration or expiration dates (like 'Inicio de Actividades' or 'Vto CAE').
            2. Product names are clean of barcodes (e.g. 779...), quantity multipliers (like '(1x3.100)' or '2 x 1.500'), and prices.
            3. Interpret the price decimal/thousands separator correctly. If it says '3.100' or '6.600' in Argentina, it means 3100.0 or 6600.0 respectively.
            4. Output ONLY valid JSON matching this schema. Do not output markdown backticks or any introductory text.
        """.trimIndent()

        val request = GroqChatRequest(
            model = "llama-3.3-70b-versatile",
            messages = listOf(
                GroqMessage("system", systemPrompt),
                GroqMessage("user", "Here is the raw ticket text:\n$rawText")
            ),
            responseFormat = GroqResponseFormat("json_object")
        )

        val response = apiService.getChatCompletion(
            authHeader = "Bearer $apiKey",
            request = request
        )

        val jsonContent = response.choices.firstOrNull()?.message?.content ?: throw Exception("Empty response from Groq")
        
        return Gson().fromJson(jsonContent, ParsedTicket::class.java)
    }
}
