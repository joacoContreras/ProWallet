package com.undef.prowallet.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.undef.prowallet.data.dao.CategoryDao
import com.undef.prowallet.data.dao.ProductDao
import com.undef.prowallet.data.dao.PurchaseDao
import com.undef.prowallet.data.dao.PurchasedItemDao
import com.undef.prowallet.data.dao.UserDao

@Database(
    entities = [
        UserEntity::class,
        PurchaseEntity::class,
        ProductEntity::class,
        PurchasedItemEntity::class,
        CategoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ProWalletDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun purchaseDao(): PurchaseDao
    abstract fun productDao(): ProductDao
    abstract fun purchasedItemDao(): PurchasedItemDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile   // cualquier hilo ve el valor actualizado de INSTANCE inmediatamente
        private var INSTANCE: ProWalletDatabase? = null

        fun getInstance(context: Context): ProWalletDatabase =
            INSTANCE ?: synchronized(this) { // si dos hilos llegan al mismo tiempo, solo uno entra y crea la instancia
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext, // evita memory leaks, no retiene referencias a Activities.
                    ProWalletDatabase::class.java,
                    "prowallet.db"
                ).build().also { INSTANCE = it }
            }
    }
}
