package com.undef.prowallet.data.dao

import androidx.room.*
import com.undef.prowallet.data.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: ProductEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<ProductEntity>)

    @Update
    suspend fun update(product: ProductEntity)

    @Delete
    suspend fun delete(product: ProductEntity)

    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Int): ProductEntity?

    @Query("SELECT * FROM products WHERE code = :code LIMIT 1")
    suspend fun getProductByCode(code: String): ProductEntity?

    @Query("SELECT * FROM products")
    suspend fun getAllProductsOnce(): List<ProductEntity>
}
