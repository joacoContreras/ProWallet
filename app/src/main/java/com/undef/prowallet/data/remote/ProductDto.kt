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

data class PreciosClarosProductDto(
    @SerializedName("id") val id: String?,
    @SerializedName("nombre") val nombre: String?,
    @SerializedName("marca") val marca: String?,
    @SerializedName("presentacion") val presentacion: String?,
    @SerializedName("precioMin") val precioMin: Double?,
    @SerializedName("precioMax") val precioMax: Double?,
    @SerializedName("cantSucursalesDisponible") val sucursalesDisponibles: Int?
)

data class PreciosClarosResponse(
    @SerializedName("productos") val productos: List<PreciosClarosProductDto>
)
