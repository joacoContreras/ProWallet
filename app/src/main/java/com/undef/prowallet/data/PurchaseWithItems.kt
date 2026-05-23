package com.undef.prowallet.data

import androidx.room.Embedded
import androidx.room.Relation

data class PurchaseWithItems(
    @Embedded val purchase: PurchaseEntity, // Incluye todos los campos de PurchaseEntity directamente
    @Relation(                             // Indica como hacer el join automaticamente
        parentColumn = "id",
        entityColumn = "purchase_id"
    )
    val items: List<PurchasedItemEntity>
)
