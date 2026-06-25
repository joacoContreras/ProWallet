package com.undef.prowallet.data.ocr

import com.undef.prowallet.data.remote.RetrofitClient
import com.undef.prowallet.domain.Purchase
import kotlinx.coroutines.runBlocking
import org.junit.Test

class NetworkTest {

    @Test
    fun testCrearCompraNetwork() {
        val compra = Purchase(
            id = "",
            storeName = "Test Store",
            date = "06/25/26",
            time = "14:20",
            totalAmount = 1000.0,
            category = "Other",
            products = emptyList()
        )
        try {
            val response = runBlocking { RetrofitClient.apiService.crearCompra(compra) }
            println("Response code: ${response.code()}")
            println("Response body: ${response.body()}")
            println("Response errorBody: ${response.errorBody()?.string()}")
        } catch (e: Exception) {
            println("Exception: ${e.message}")
            e.printStackTrace()
        }
    }
}
