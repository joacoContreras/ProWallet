package com.undef.prowallet.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "type") val type: String,
    @ColumnInfo(name = "last_four") val lastFour: String,
    @ColumnInfo(name = "is_primary") val isPrimary: Boolean = false,
    @ColumnInfo(name = "user_email") val userEmail: String = "",
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "is_dirty") val isDirty: Boolean = false,
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean = false
)
