package com.undef.prowallet.data.dao

import androidx.room.*
import com.undef.prowallet.data.PurchasedItemEntity
import com.undef.prowallet.data.PurchasedItemWithProduct
import kotlinx.coroutines.flow.Flow

@Dao
interface PurchasedItemDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(item: PurchasedItemEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(items: List<PurchasedItemEntity>)

    @Delete
    suspend fun delete(item: PurchasedItemEntity)

    @Query("DELETE FROM purchase_items WHERE purchase_id = :purchaseId")
    suspend fun deleteByPurchaseId(purchaseId: Int)

    @Query("SELECT * FROM purchase_items WHERE purchase_id = :purchaseId")
    fun getItemsByPurchaseId(purchaseId: Int): Flow<List<PurchasedItemEntity>>

    @Query("SELECT * FROM purchase_items WHERE purchase_id = :purchaseId")
    suspend fun getItemsByPurchaseIdOnce(purchaseId: Int): List<PurchasedItemEntity>

    @Query("""
        SELECT pi.id, pi.purchase_id, pi.product_id, pi.quantity, pi.price,
               p.name, p.description, p.code
        FROM purchase_items pi
        INNER JOIN products p ON pi.product_id = p.id
        WHERE pi.purchase_id = :purchaseId
    """)
    suspend fun getItemsWithProductsByPurchaseId(purchaseId: Int): List<PurchasedItemWithProduct>
}
