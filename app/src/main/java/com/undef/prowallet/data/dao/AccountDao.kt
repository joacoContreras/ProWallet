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

    @Query("UPDATE accounts SET is_primary = 0 WHERE user_email = :userEmail")
    suspend fun clearPrimary(userEmail: String)

    @Query("SELECT * FROM accounts WHERE (user_email = :userEmail OR user_email = '') AND is_deleted = 0 ORDER BY is_primary DESC, name ASC")
    fun getAll(userEmail: String): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE user_email = :userEmail AND is_dirty = 1")
    suspend fun getDirty(userEmail: String): List<AccountEntity>

    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun getById(id: Int): AccountEntity?
}
