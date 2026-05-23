package com.undef.prowallet.data

import androidx.room.ColumnInfo

data class StoreTotal(
    @ColumnInfo(name = "store_name") val storeName: String,
    val total: Double
)
