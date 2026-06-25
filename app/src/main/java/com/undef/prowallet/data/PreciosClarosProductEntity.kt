package com.undef.prowallet.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "precios_claros_cache")
data class PreciosClarosProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "query") val query: String,
    @ColumnInfo(name = "api_product_id") val apiProductId: String?,
    @ColumnInfo(name = "nombre") val nombre: String?,
    @ColumnInfo(name = "marca") val marca: String?,
    @ColumnInfo(name = "presentacion") val presentacion: String?,
    @ColumnInfo(name = "precio_min") val precioMin: Double?,
    @ColumnInfo(name = "precio_max") val precioMax: Double?,
    @ColumnInfo(name = "sucursales_disponibles") val sucursalesDisponibles: Int?,
    @ColumnInfo(name = "timestamp") val timestamp: Long
)
