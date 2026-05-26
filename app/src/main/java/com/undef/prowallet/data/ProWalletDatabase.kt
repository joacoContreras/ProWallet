package com.undef.prowallet.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.undef.prowallet.BuildConfig
import com.undef.prowallet.data.dao.AccountDao
import com.undef.prowallet.data.dao.CategoryDao
import com.undef.prowallet.data.dao.FixedExpenseDao
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
        CategoryEntity::class,
        FixedExpenseEntity::class,
        AccountEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class ProWalletDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun purchaseDao(): PurchaseDao
    abstract fun productDao(): ProductDao
    abstract fun purchasedItemDao(): PurchasedItemDao
    abstract fun categoryDao(): CategoryDao
    abstract fun fixedExpenseDao(): FixedExpenseDao
    abstract fun accountDao(): AccountDao

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

        // v4→v5: add fixed_expenses table.
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS fixed_expenses (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        amount REAL NOT NULL,
                        category TEXT NOT NULL,
                        frequency TEXT NOT NULL
                    )
                """.trimIndent())
            }
        }

        // v5→v6: add accounts table.
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS accounts (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        type TEXT NOT NULL,
                        last_four TEXT NOT NULL,
                        is_primary INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())
            }
        }

        fun getInstance(context: Context): ProWalletDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    ProWalletDatabase::class.java,
                    "prowallet.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
                    .apply { if (BuildConfig.DEBUG) fallbackToDestructiveMigration(dropAllTables = true) }
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
