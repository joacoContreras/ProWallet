package com.undef.prowallet.data.remote

import com.google.gson.annotations.SerializedName

data class ProductDto(
    @SerializedName("nombre") val nombre: String,
    @SerializedName("descripcion") val descripcion: String,
    @SerializedName("precio_promedio") val precioPromedio: Double
)

data class ProductsResponse(
    @SerializedName("productos") val productos: List<ProductDto>
)
