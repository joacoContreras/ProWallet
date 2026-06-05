package com.undef.prowallet.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface PreciosClarosApiService {

    @GET("prod/productos")
    suspend fun getProductos(
        @Query("string") query: String,
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("limit") limit: Int    ): PreciosClarosResponse
}
