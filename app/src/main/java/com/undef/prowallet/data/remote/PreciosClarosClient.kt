package com.undef.prowallet.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object PreciosClarosClient {

    private const val BASE_URL = "https://d3e6htiiul5ek9.cloudfront.net/"

    val service: PreciosClarosApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PreciosClarosApiService::class.java)
    }
}
