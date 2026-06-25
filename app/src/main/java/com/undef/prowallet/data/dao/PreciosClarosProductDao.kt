package com.undef.prowallet.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.undef.prowallet.data.PreciosClarosProductEntity

@Dao
interface PreciosClarosProductDao {

    @Query("SELECT * FROM precios_claros_cache WHERE `query` = :query")
    suspend fun getProductsByQuery(query: String): List<PreciosClarosProductEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<PreciosClarosProductEntity>)

    @Query("DELETE FROM precios_claros_cache WHERE `query` = :query")
    suspend fun deleteByQuery(query: String)

    @Query("DELETE FROM precios_claros_cache")
    suspend fun clearAll()
}
