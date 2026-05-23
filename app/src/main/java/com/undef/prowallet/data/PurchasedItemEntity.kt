package com.undef.prowallet.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "purchase_items",
    primaryKeys = ["purchase_id","product_id"],
    foreignKeys = [
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["product_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PurchaseEntity::class,
            parentColumns = ["id"],
            childColumns = ["purchase_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("purchase_id"), Index("product_id")]
)

data class PurchasedItemEntity(
    @ColumnInfo(name = "purchase_id") val purchaseId: Int,
    @ColumnInfo(name = "product_id") val productId: Int,
    @ColumnInfo(name = "quantity") val quantity: Int = 0,
    @ColumnInfo(name = "price") val price: Double = 0.0,
)
