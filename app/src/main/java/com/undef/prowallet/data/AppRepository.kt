package com.undef.prowallet.data

import android.content.Context
import com.undef.prowallet.domain.Product
import com.undef.prowallet.domain.Purchase
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

    val purchasesFlow: Flow<List<Purchase>> = purchaseDao.getAllPurchases()
        .map { entities ->
            entities.map { entity ->
                val categoryName = entity.categoryId
                    ?.let { categoryDao.getCategoryById(it)?.name }
                    ?: "Other"
                val items = purchasedItemDao.getItemsWithProductsByPurchaseId(entity.id)
                entity.toDomain(categoryName, items)
            }
        }

    suspend fun savePurchase(purchase: Purchase) {
        val categoryId = categoryDao.getCategoryByName(purchase.category)?.id
            ?: categoryDao.insert(CategoryEntity(name = purchase.category)).toInt()

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
                ?: productDao.insert(
                    ProductEntity(
                        name = product.name,
                        description = product.description,
                        code = product.code
                    )
                ).toInt()
            purchasedItemDao.insert(
                PurchasedItemEntity(
                    purchaseId = purchaseId,
                    productId = productId,
                    quantity = 1,
                    price = product.price
                )
            )
        }
    }

    suspend fun deletePurchase(id: Int) {
        purchaseDao.deleteById(id)
    }

    suspend fun getPurchaseById(id: Int): Purchase? {
        val entity = purchaseDao.getPurchaseById(id) ?: return null
        val categoryName = entity.categoryId
            ?.let { categoryDao.getCategoryById(it)?.name }
            ?: "Other"
        val items = purchasedItemDao.getItemsWithProductsByPurchaseId(entity.id)
        return entity.toDomain(categoryName, items)
    }

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

    private fun PurchaseEntity.toDomain(
        categoryName: String,
        items: List<PurchasedItemWithProduct>
    ): Purchase {
        val date = Date(timestamp)
        return Purchase(
            id = id.toString(),
            storeName = storeName,
            date = SimpleDateFormat("MM/dd/yy", Locale.getDefault()).format(date),
            time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(date),
            totalAmount = amount,
            category = categoryName,
            products = items.map { it.toProduct() },
            ticketImageUri = ticketImagePath
        )
    }

    private fun PurchasedItemWithProduct.toProduct() = Product(
        id = item.productId.toString(),
        code = productCode,
        name = productName,
        description = productDescription ?: "",
        price = item.price
    )
}
