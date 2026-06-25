package com.undef.prowallet.data.dao

import androidx.room.*
import com.undef.prowallet.data.FixedExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FixedExpenseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: FixedExpenseEntity): Long

    @Update
    suspend fun update(expense: FixedExpenseEntity)

    @Query("DELETE FROM fixed_expenses WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("SELECT * FROM fixed_expenses WHERE (user_email = :userEmail OR user_email = '') AND is_deleted = 0 ORDER BY name ASC")
    fun getAll(userEmail: String): Flow<List<FixedExpenseEntity>>

    @Query("SELECT * FROM fixed_expenses WHERE user_email = :userEmail AND is_dirty = 1")
    suspend fun getDirty(userEmail: String): List<FixedExpenseEntity>

    @Query("SELECT * FROM fixed_expenses WHERE id = :id")
    suspend fun getById(id: Int): FixedExpenseEntity?
}
