package com.undef.prowallet.data.dao

import androidx.room.*
import com.undef.prowallet.data.PurchaseEntity
import com.undef.prowallet.data.PurchaseWithItems
import com.undef.prowallet.data.StoreTotal
import kotlinx.coroutines.flow.Flow

@Dao
interface PurchaseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(purchase: PurchaseEntity): Long

    @Update
    suspend fun update(purchase: PurchaseEntity)

    @Delete
    suspend fun delete(purchase: PurchaseEntity)

    @Query("DELETE FROM purchases WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("SELECT * FROM purchases ORDER BY timestamp DESC")
    fun getAllPurchases(): Flow<List<PurchaseEntity>>

    @Transaction
    @Query("SELECT * FROM purchases ORDER BY timestamp DESC")
    fun getAllPurchasesWithItems(): Flow<List<PurchaseWithItems>>

    @Transaction
    @Query("SELECT * FROM purchases WHERE id = :id")
    suspend fun getPurchaseWithItemsById(id: Int): PurchaseWithItems?

    @Query("SELECT * FROM purchases WHERE id = :id")
    suspend fun getPurchaseById(id: Int): PurchaseEntity?

    @Query("SELECT * FROM purchases WHERE category_id = :categoryId ORDER BY timestamp DESC")
    fun getPurchasesByCategory(categoryId: Int): Flow<List<PurchaseEntity>>

    @Query("SELECT * FROM purchases WHERE timestamp >= :from AND timestamp <= :to ORDER BY timestamp DESC")
    fun getPurchasesInRange(from: Long, to: Long): Flow<List<PurchaseEntity>>

    @Query("SELECT * FROM purchases ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentPurchases(limit: Int): Flow<List<PurchaseEntity>>

    @Query("SELECT SUM(amount) FROM purchases")
    fun getTotalSpent(): Flow<Double?>

    @Query("SELECT SUM(amount) FROM purchases WHERE timestamp >= :from AND timestamp <= :to")
    fun getTotalSpentInRange(from: Long, to: Long): Flow<Double?>

    @Query("SELECT store_name, SUM(amount) AS total FROM purchases GROUP BY store_name ORDER BY total DESC LIMIT :limit")
    fun getTopStores(limit: Int): Flow<List<StoreTotal>>
}
