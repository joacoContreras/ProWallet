package com.undef.prowallet.data.remote

import com.undef.prowallet.domain.Purchase
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface PurchaseApiService {

    @POST("compras")
    suspend fun crearCompra(@Body compra: Purchase): Response<PurchaseResponseDto>
}

data class PurchaseResponseDto(
    val id: String,
    val success: Boolean
)
