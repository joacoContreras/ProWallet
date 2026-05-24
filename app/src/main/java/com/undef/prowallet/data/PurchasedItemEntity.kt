package com.undef.prowallet.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "purchase_items",
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
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "purchase_id") val purchaseId: Int,
    @ColumnInfo(name = "product_id") val productId: Int,
    @ColumnInfo(name = "quantity") val quantity: Int = 1,
    @ColumnInfo(name = "price") val price: Double = 0.0
)
