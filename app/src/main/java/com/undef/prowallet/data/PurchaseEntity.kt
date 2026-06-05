package com.undef.prowallet.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(
    tableName = "purchases",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.SET_NULL  // si se borra la categoría, el campo queda null
        )
    ],
    indices = [Index("category_id")]
)
data class PurchaseEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "category_id") val categoryId: Int?,
    @ColumnInfo(name = "amount") val amount: Double,
    @ColumnInfo(name = "store_name") val storeName: String,
    @ColumnInfo(name = "description") val description: String,
    @ColumnInfo(name = "timestamp") val timestamp: Long,
    @ColumnInfo(name = "ticket_image_path") val ticketImagePath: String? = null,
    @ColumnInfo(name = "latitude") val latitude: Double? = null,
    @ColumnInfo(name = "longitude") val longitude: Double? = null
)
