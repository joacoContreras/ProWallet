package com.undef.prowallet.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.undef.prowallet.BuildConfig
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
    version = 4,
    exportSchema = false
)
abstract class ProWalletDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun purchaseDao(): PurchaseDao
    abstract fun productDao(): ProductDao
    abstract fun purchasedItemDao(): PurchasedItemDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: ProWalletDatabase? = null

        // v1→v2: purchase_items PK changed from composite (purchase_id, product_id)
        //         to auto-generated id; quantity default fixed 0→1.
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE purchase_items_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        purchase_id INTEGER NOT NULL,
                        product_id INTEGER NOT NULL,
                        quantity INTEGER NOT NULL DEFAULT 1,
                        price REAL NOT NULL DEFAULT 0.0,
                        FOREIGN KEY (purchase_id) REFERENCES purchases(id) ON DELETE CASCADE,
                        FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("""
                    INSERT INTO purchase_items_new (purchase_id, product_id, quantity, price)
                    SELECT purchase_id, product_id, quantity, price FROM purchase_items
                """.trimIndent())
                db.execSQL("DROP TABLE purchase_items")
                db.execSQL("ALTER TABLE purchase_items_new RENAME TO purchase_items")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_purchase_items_purchase_id ON purchase_items (purchase_id)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_purchase_items_product_id ON purchase_items (product_id)")
            }
        }

        // v2→v3: categories.name gets a UNIQUE constraint.
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_categories_name ON categories (name)")
            }
        }

        // v3→v4: products.code gets a UNIQUE constraint.
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_products_code ON products (code)")
            }
        }

        fun getInstance(context: Context): ProWalletDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    ProWalletDatabase::class.java,
                    "prowallet.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .apply { if (BuildConfig.DEBUG) fallbackToDestructiveMigration(dropAllTables = true) }
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
