package com.undef.prowallet.data

import android.content.Context
import com.undef.prowallet.data.remote.ProductDto
import com.undef.prowallet.data.remote.RetrofitClient
import com.undef.prowallet.domain.Product
import com.undef.prowallet.domain.Purchase
import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AppRepository(context: Context) {

    private val db = ProWalletDatabase.getInstance(context)
    private val purchaseDao = db.purchaseDao()
    private val productDao = db.productDao()
    private val purchasedItemDao = db.purchasedItemDao()
    private val categoryDao = db.categoryDao()
    private val fixedExpenseDao = db.fixedExpenseDao()
    private val accountDao = db.accountDao()

    val fixedExpensesFlow: Flow<List<FixedExpenseEntity>> = fixedExpenseDao.getAll()
    val accountsFlow: Flow<List<AccountEntity>> = accountDao.getAll()

    suspend fun addAccount(name: String, type: String, lastFour: String, isPrimary: Boolean): Boolean {
        if (name.isBlank()) return false
        if (isPrimary) accountDao.clearPrimary()
        return accountDao.insert(AccountEntity(name = name, type = type, lastFour = lastFour, isPrimary = isPrimary)) != -1L
    }

    suspend fun updateAccount(id: Int, name: String, type: String, lastFour: String, isPrimary: Boolean) {
        if (name.isBlank()) return
        if (isPrimary) accountDao.clearPrimary()
        accountDao.update(AccountEntity(id = id, name = name, type = type, lastFour = lastFour, isPrimary = isPrimary))
    }

    suspend fun deleteAccount(id: Int) = accountDao.deleteById(id)

    suspend fun addFixedExpense(name: String, amount: Double, category: String, frequency: String): Boolean {
        if (name.isBlank() || amount <= 0) return false
        return fixedExpenseDao.insert(FixedExpenseEntity(name = name, amount = amount, category = category, frequency = frequency)) != -1L
    }

    suspend fun updateFixedExpense(id: Int, name: String, amount: Double, category: String, frequency: String) {
        if (name.isBlank() || amount <= 0) return
        fixedExpenseDao.update(FixedExpenseEntity(id = id, name = name, amount = amount, category = category, frequency = frequency))
    }

    suspend fun deleteFixedExpense(id: Int) = fixedExpenseDao.deleteById(id)

    // @Transaction query tracks purchases + purchase_items — no race condition.
    // Per emission: 1 query for categories, 1 for products → 3 total, no N+1.
    val categoriesFlow: Flow<List<CategoryEntity>> = categoryDao.getAllCategories()

    suspend fun addCategory(name: String): Boolean {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return false
        return categoryDao.insert(CategoryEntity(name = trimmed)) != -1L
    }

    suspend fun deleteCategory(id: Int) = categoryDao.deleteById(id)

    suspend fun updateCategory(id: Int, newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isNotBlank()) categoryDao.updateName(id, trimmed)
    }

    val purchasesFlow: Flow<List<Purchase>> = purchaseDao.getAllPurchasesWithItems()
        .map { purchasesWithItems ->
            val categoryMap = categoryDao.getAllCategoriesOnce().associateBy { it.id }
            val productMap = productDao.getAllProductsOnce().associateBy { it.id }
            purchasesWithItems.map { it.toDomain(categoryMap, productMap) }
        }

    suspend fun savePurchase(purchase: Purchase): Int {
        return db.withTransaction {
            val categoryId = categoryDao.getCategoryByName(purchase.category)?.id
                ?: run {
                    val inserted = categoryDao.insert(CategoryEntity(name = purchase.category))
                    if (inserted != -1L) inserted.toInt()
                    else categoryDao.getCategoryByName(purchase.category)!!.id
                }

            val purchaseId = purchaseDao.insert(
                PurchaseEntity(
                    categoryId = categoryId,
                    amount = purchase.totalAmount,
                    storeName = purchase.storeName,
                    description = "",
                    timestamp = parseTimestamp(purchase.date, purchase.time),
                    ticketImagePath = purchase.ticketImageUri
                )
            ).toInt()

            purchase.products.forEach { product ->
                val productId = productDao.getProductByCode(product.code)?.id
                    ?: run {
                        val inserted = productDao.insert(
                            ProductEntity(
                                name = product.name,
                                description = product.description,
                                code = product.code
                            )
                        )
                        if (inserted != -1L) inserted.toInt()
                        else productDao.getProductByCode(product.code)!!.id
                    }
                purchasedItemDao.insert(
                    PurchasedItemEntity(
                        purchaseId = purchaseId,
                        productId = productId,
                        quantity = 1,
                        price = product.price
                    )
                )
            }
            purchaseId
        }
    }

    suspend fun updatePurchase(id: Int, purchase: Purchase) {
        db.withTransaction {
            val categoryId = categoryDao.getCategoryByName(purchase.category)?.id
                ?: run {
                    val inserted = categoryDao.insert(CategoryEntity(name = purchase.category))
                    if (inserted != -1L) inserted.toInt()
                    else categoryDao.getCategoryByName(purchase.category)!!.id
                }

            purchaseDao.update(
                PurchaseEntity(
                    id = id,
                    categoryId = categoryId,
                    amount = purchase.totalAmount,
                    storeName = purchase.storeName,
                    description = "",
                    timestamp = parseTimestamp(purchase.date, purchase.time),
                    ticketImagePath = purchase.ticketImageUri
                )
            )

            purchasedItemDao.deleteByPurchaseId(id)

            purchase.products.forEach { product ->
                val productId = productDao.getProductByCode(product.code)?.id
                    ?: run {
                        val inserted = productDao.insert(
                            ProductEntity(
                                name = product.name,
                                description = product.description,
                                code = product.code
                            )
                        )
                        if (inserted != -1L) inserted.toInt()
                        else productDao.getProductByCode(product.code)!!.id
                    }
                purchasedItemDao.insert(
                    PurchasedItemEntity(
                        purchaseId = id,
                        productId = productId,
                        quantity = 1,
                        price = product.price
                    )
                )
            }
        }
    }

    suspend fun deletePurchase(id: Int) {
        purchaseDao.deleteById(id)
    }

    suspend fun getPurchaseById(id: Int): Purchase? {
        val pwi = purchaseDao.getPurchaseWithItemsById(id) ?: return null
        val categoryMap = categoryDao.getAllCategoriesOnce().associateBy { it.id }
        val productMap = productDao.getAllProductsOnce().associateBy { it.id }
        return pwi.toDomain(categoryMap, productMap)
    }

    suspend fun getApiProducts(): List<ProductDto> =
        RetrofitClient.productApiService.getProducts().productos

    suspend fun seedDefaultCategories() {
        listOf("Groceries", "Transport", "Dining", "Coffee", "Other").forEach { name ->
            if (categoryDao.getCategoryByName(name) == null) {
                categoryDao.insert(CategoryEntity(name = name))
            }
        }
    }

    private fun parseTimestamp(date: String, time: String): Long {
        return try {
            SimpleDateFormat("MM/dd/yy HH:mm", Locale.getDefault())
                .parse("$date $time")?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    private fun PurchaseWithItems.toDomain(
        categoryMap: Map<Int, CategoryEntity>,
        productMap: Map<Int, ProductEntity>
    ): Purchase {
        val date = Date(purchase.timestamp)
        val categoryName = purchase.categoryId?.let { categoryMap[it]?.name } ?: "Other"
        val products = items.mapNotNull { item ->
            productMap[item.productId]?.let { p ->
                Product(
                    id = item.productId.toString(),
                    code = p.code,
                    name = p.name,
                    description = p.description ?: "",
                    price = item.price
                )
            }
        }
        return Purchase(
            id = purchase.id.toString(),
            storeName = purchase.storeName,
            date = SimpleDateFormat("MM/dd/yy", Locale.getDefault()).format(date),
            time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(date),
            totalAmount = purchase.amount,
            category = categoryName,
            products = products,
            ticketImageUri = purchase.ticketImagePath,
            timestampMs = purchase.timestamp
        )
    }
}
