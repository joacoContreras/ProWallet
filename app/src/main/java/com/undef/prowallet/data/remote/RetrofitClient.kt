package com.undef.prowallet.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    val productApiService: ProductApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.npoint.io/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ProductApiService::class.java)
    }

    val apiService: PurchaseApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.npoint.io/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PurchaseApiService::class.java)
    }

    val syncApiService: SyncApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.prowallet.undef.edu.ar/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SyncApiService::class.java)
    }
}
