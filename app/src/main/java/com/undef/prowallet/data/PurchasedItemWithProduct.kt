package com.undef.prowallet.data

import androidx.room.ColumnInfo
import androidx.room.Embedded

data class PurchasedItemWithProduct(
    @Embedded val item: PurchasedItemEntity,
    @ColumnInfo(name = "name") val productName: String,
    @ColumnInfo(name = "description") val productDescription: String?,
    @ColumnInfo(name = "code") val productCode: String
)
