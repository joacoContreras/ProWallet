package com.undef.prowallet.data.remote

import retrofit2.http.GET

interface ProductApiService {

    @GET("5ea678c319a23faa10ab")
    suspend fun getProducts(): ProductsResponse
}
