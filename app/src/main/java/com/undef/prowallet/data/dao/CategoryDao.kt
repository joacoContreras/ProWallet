package com.undef.prowallet.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.undef.prowallet.data.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(category: CategoryEntity): Long

    @Delete
    suspend fun delete(category: CategoryEntity)

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategoryById(id: Int): CategoryEntity?

    @Query("SELECT * FROM categories WHERE name = :name AND (user_email = :userEmail OR user_email = '') AND is_deleted = 0 LIMIT 1")
    suspend fun getCategoryByName(name: String, userEmail: String): CategoryEntity?

    @Query("SELECT * FROM categories WHERE (user_email = :userEmail OR user_email = '') AND is_deleted = 0 ORDER BY name ASC")
    fun getAllCategories(userEmail: String): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE (user_email = :userEmail OR user_email = '') AND is_deleted = 0")
    suspend fun getAllCategoriesOnce(userEmail: String): List<CategoryEntity>

    @Query("UPDATE categories SET name = :newName, updated_at = :updatedAt, is_dirty = 1 WHERE id = :id")
    suspend fun updateName(id: Int, newName: String, updatedAt: Long = System.currentTimeMillis())

    @Query("SELECT * FROM categories WHERE user_email = :userEmail AND is_dirty = 1")
    suspend fun getDirty(userEmail: String): List<CategoryEntity>

    @Update
    suspend fun update(category: CategoryEntity)
}
