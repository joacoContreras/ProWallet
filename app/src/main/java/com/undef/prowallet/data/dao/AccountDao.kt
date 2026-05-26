package com.undef.prowallet.data.dao

import androidx.room.*
import com.undef.prowallet.data.AccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(account: AccountEntity): Long

    @Update
    suspend fun update(account: AccountEntity)

    @Query("DELETE FROM accounts WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("SELECT * FROM accounts ORDER BY is_primary DESC, name ASC")
    fun getAll(): Flow<List<AccountEntity>>

    @Query("UPDATE accounts SET is_primary = 0")
    suspend fun clearPrimary()
}
