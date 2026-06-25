package com.undef.prowallet.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fixed_expenses")
data class FixedExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "amount") val amount: Double,
    @ColumnInfo(name = "category") val category: String,
    @ColumnInfo(name = "frequency") val frequency: String,
    @ColumnInfo(name = "user_email") val userEmail: String = "",
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "is_dirty") val isDirty: Boolean = false,
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean = false
)
