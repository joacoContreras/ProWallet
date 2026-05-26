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

    @Query("SELECT * FROM fixed_expenses ORDER BY name ASC")
    fun getAll(): Flow<List<FixedExpenseEntity>>
}
