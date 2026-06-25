package com.undef.prowallet.data

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.room.withTransaction
import com.undef.prowallet.data.remote.PreciosClarosClient
import com.undef.prowallet.data.remote.PreciosClarosProductDto
import com.undef.prowallet.data.remote.RetrofitClient
import com.undef.prowallet.domain.Product
import com.undef.prowallet.domain.Purchase
import com.undef.prowallet.util.SessionManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AppRepository(private val context: Context) {

    companion object {
        const val CATEGORY_FALLBACK = "Other"
        val DEFAULT_CATEGORIES = listOf("Groceries", "Transport", "Dining", "Coffee", "Other")
    }

    private val db = ProWalletDatabase.getInstance(context)
    private val purchaseDao = db.purchaseDao()
    private val productDao = db.productDao()
    private val purchasedItemDao = db.purchasedItemDao()
    private val categoryDao = db.categoryDao()
    private val fixedExpenseDao = db.fixedExpenseDao()
    private val accountDao = db.accountDao()
    private val preciosClarosProductDao = db.preciosClarosProductDao()
    private val sessionManager = SessionManager(context)

    suspend fun getUserEmail(): String = sessionManager.email.first() ?: ""

    @OptIn(ExperimentalCoroutinesApi::class)
    val fixedExpensesFlow: Flow<List<FixedExpenseEntity>> = sessionManager.email
        .flatMapLatest { email ->
            fixedExpenseDao.getAll(email ?: "")
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    val accountsFlow: Flow<List<AccountEntity>> = sessionManager.email
        .flatMapLatest { email ->
            accountDao.getAll(email ?: "")
        }

    suspend fun addAccount(name: String, type: String, lastFour: String, isPrimary: Boolean): Boolean {
        if (name.isBlank()) return false
        val email = getUserEmail()
        if (isPrimary) accountDao.clearPrimary(email)
        return accountDao.insert(
            AccountEntity(
                name = name,
                type = type,
                lastFour = lastFour,
                isPrimary = isPrimary,
                userEmail = email,
                updatedAt = System.currentTimeMillis(),
                isDirty = true,
                isDeleted = false
            )
        ) != -1L
    }

    suspend fun updateAccount(id: Int, name: String, type: String, lastFour: String, isPrimary: Boolean) {
        if (name.isBlank()) return
        val email = getUserEmail()
        if (isPrimary) accountDao.clearPrimary(email)
        accountDao.update(
            AccountEntity(
                id = id,
                name = name,
                type = type,
                lastFour = lastFour,
                isPrimary = isPrimary,
                userEmail = email,
                updatedAt = System.currentTimeMillis(),
                isDirty = true,
                isDeleted = false
            )
        )
    }

    suspend fun deleteAccount(id: Int) {
        val account = accountDao.getById(id) ?: return
        accountDao.update(
            account.copy(
                isDeleted = true,
                isDirty = true,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun addFixedExpense(name: String, amount: Double, category: String, frequency: String): Boolean {
        if (name.isBlank() || amount <= 0) return false
        val email = getUserEmail()
        return fixedExpenseDao.insert(
            FixedExpenseEntity(
                name = name,
                amount = amount,
                category = category,
                frequency = frequency,
                userEmail = email,
                updatedAt = System.currentTimeMillis(),
                isDirty = true,
                isDeleted = false
            )
        ) != -1L
    }

    suspend fun updateFixedExpense(id: Int, name: String, amount: Double, category: String, frequency: String) {
        if (name.isBlank() || amount <= 0) return
        val email = getUserEmail()
        fixedExpenseDao.update(
            FixedExpenseEntity(
                id = id,
                name = name,
                amount = amount,
                category = category,
                frequency = frequency,
                userEmail = email,
                updatedAt = System.currentTimeMillis(),
                isDirty = true,
                isDeleted = false
            )
        )
    }

    suspend fun deleteFixedExpense(id: Int) {
        val expense = fixedExpenseDao.getById(id) ?: return
        fixedExpenseDao.update(
            expense.copy(
                isDeleted = true,
                isDirty = true,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    // @Transaction query tracks purchases + purchase_items — no race condition.
    // Per emission: 1 query for categories, 1 for products → 3 total, no N+1.
    @OptIn(ExperimentalCoroutinesApi::class)
    val categoriesFlow: Flow<List<CategoryEntity>> = sessionManager.email
        .flatMapLatest { email ->
            categoryDao.getAllCategories(email ?: "")
        }

    suspend fun addCategory(name: String): Boolean {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return false
        val email = getUserEmail()
        return categoryDao.insert(
            CategoryEntity(
                name = trimmed,
                userEmail = email,
                updatedAt = System.currentTimeMillis(),
                isDirty = true,
                isDeleted = false
            )
        ) != -1L
    }

    suspend fun deleteCategory(id: Int) {
        val category = categoryDao.getCategoryById(id) ?: return
        categoryDao.update(
            category.copy(
                isDeleted = true,
                isDirty = true,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun updateCategory(id: Int, newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isNotBlank()) {
            try {
                categoryDao.updateName(id, trimmed, System.currentTimeMillis())
            } catch (_: SQLiteConstraintException) {
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val purchasesFlow: Flow<List<Purchase>> = sessionManager.email
        .flatMapLatest { email ->
            val userEmail = email ?: ""
            purchaseDao.getAllPurchasesWithItems(userEmail).map { purchasesWithItems ->
                val categoryMap = categoryDao.getAllCategoriesOnce(userEmail).associateBy { it.id }
                val productMap = productDao.getAllProductsOnce().associateBy { it.id }
                purchasesWithItems.map { it.toDomain(categoryMap, productMap) }
            }
        }

    suspend fun guardarCompra(compra: Purchase): Int = savePurchase(compra)

    suspend fun savePurchase(purchase: Purchase): Int {
        val email = getUserEmail()
        return db.withTransaction {
            val categoryId = categoryDao.getCategoryByName(purchase.category, email)?.id
                ?: run {
                    val inserted = categoryDao.insert(
                        CategoryEntity(
                            name = purchase.category,
                            userEmail = email,
                            updatedAt = System.currentTimeMillis(),
                            isDirty = true,
                            isDeleted = false
                        )
                    )
                    if (inserted != -1L) inserted.toInt()
                    else categoryDao.getCategoryByName(purchase.category, email)!!.id
                }

            val purchaseId = purchaseDao.insert(
                PurchaseEntity(
                    categoryId = categoryId,
                    amount = purchase.totalAmount,
                    storeName = purchase.storeName,
                    description = "",
                    timestamp = parseTimestamp(purchase.date, purchase.time),
                    ticketImagePath = purchase.ticketImageUri,
                    latitude = purchase.latitude,
                    longitude = purchase.longitude,
                    userEmail = email,
                    updatedAt = System.currentTimeMillis(),
                    isDirty = true,
                    isDeleted = false
                )
            ).toInt()

            purchase.products.forEach { product ->
                val existingProduct = productDao.getProductByCode(product.code)
                val productId = if (existingProduct != null) {
                    if (existingProduct.name != product.name || existingProduct.description != product.description) {
                        productDao.update(
                            existingProduct.copy(
                                name = product.name,
                                description = product.description
                            )
                        )
                    }
                    existingProduct.id
                } else {
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
        val email = getUserEmail()
        db.withTransaction {
            val categoryId = categoryDao.getCategoryByName(purchase.category, email)?.id
                ?: run {
                    val inserted = categoryDao.insert(
                        CategoryEntity(
                            name = purchase.category,
                            userEmail = email,
                            updatedAt = System.currentTimeMillis(),
                            isDirty = true,
                            isDeleted = false
                        )
                    )
                    if (inserted != -1L) inserted.toInt()
                    else categoryDao.getCategoryByName(purchase.category, email)!!.id
                }

            purchaseDao.update(
                PurchaseEntity(
                    id = id,
                    categoryId = categoryId,
                    amount = purchase.totalAmount,
                    storeName = purchase.storeName,
                    description = "",
                    timestamp = parseTimestamp(purchase.date, purchase.time),
                    ticketImagePath = purchase.ticketImageUri,
                    latitude = purchase.latitude,
                    longitude = purchase.longitude,
                    userEmail = email,
                    updatedAt = System.currentTimeMillis(),
                    isDirty = true,
                    isDeleted = false
                )
            )

            purchasedItemDao.deleteByPurchaseId(id)

            purchase.products.forEach { product ->
                val existingProduct = productDao.getProductByCode(product.code)
                val productId = if (existingProduct != null) {
                    if (existingProduct.name != product.name || existingProduct.description != product.description) {
                        productDao.update(
                            existingProduct.copy(
                                name = product.name,
                                description = product.description
                            )
                        )
                    }
                    existingProduct.id
                } else {
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
        val purchase = purchaseDao.getPurchaseById(id) ?: return
        purchaseDao.update(
            purchase.copy(
                isDeleted = true,
                isDirty = true,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun getPurchaseById(id: Int): Purchase? {
        val pwi = purchaseDao.getPurchaseWithItemsById(id) ?: return null
        val email = getUserEmail()
        val categoryMap = categoryDao.getAllCategoriesOnce(email).associateBy { it.id }
        val productMap = productDao.getAllProductsOnce().associateBy { it.id }
        return pwi.toDomain(categoryMap, productMap)
    }

    // Flow reactivo: Room es la fuente de verdad. La UI observa este Flow.
    val apiProductsFlow: Flow<List<ProductEntity>> = productDao.getAllProducts()

    // Cache-first: consulta Room primero. Si está vacío, llama a Retrofit y persiste en Room.
    // El Flow apiProductsFlow emite automáticamente cuando Room se actualiza.
    suspend fun refreshApiProductsIfEmpty() {
        if (productDao.getProductCount() > 0) return
        try {
            val dtos = RetrofitClient.productApiService.getProducts().productos
            val entities = dtos.map { dto ->
                ProductEntity(
                    name = dto.nombre,
                    description = dto.descripcion,
                    code = dto.nombre.trim().lowercase(Locale.ROOT).replace(" ", "_")
                )
            }
            productDao.insertAll(entities)
        } catch (_: Exception) {
            // Sin red: tabla queda vacía, apiProductsFlow emite lista vacía sin crashear
        }
    }

    suspend fun getProductByCode(code: String): ProductEntity? {
        return productDao.getProductByCode(code)
    }

    suspend fun searchProductPrices(lat: Double, lng: Double, query: String): List<PreciosClarosProductDto> {
        val normalizedQuery = query.trim().lowercase(Locale.ROOT)
        // 1. Consultar Room primero (cache-first)
        val cached = preciosClarosProductDao.getProductsByQuery(normalizedQuery)
        // Definir tiempo de expiración: 24 horas (86400000 ms)
        val isExpired = cached.isNotEmpty() && (System.currentTimeMillis() - cached.first().timestamp > 24 * 60 * 60 * 1000)

        if (cached.isNotEmpty() && !isExpired) {
            return cached.map {
                PreciosClarosProductDto(
                    id = it.apiProductId,
                    nombre = it.nombre,
                    marca = it.marca,
                    presentacion = it.presentacion,
                    precioMin = it.precioMin,
                    precioMax = it.precioMax,
                    sucursalesDisponibles = it.sucursalesDisponibles
                )
            }
        }

        // 2. Si no hay cache o está vencido, llamar a Retrofit
        try {
            val dtos = PreciosClarosClient.service.getProductos(query = query, lat = lat, lng = lng, limit = 30).productos ?: emptyList()

            // 3. Guardar la respuesta en Room (Single Source of Truth)
            preciosClarosProductDao.deleteByQuery(normalizedQuery)
            if (dtos.isNotEmpty()) {
                val entities = dtos.map { dto ->
                    PreciosClarosProductEntity(
                        query = normalizedQuery,
                        apiProductId = dto.id,
                        nombre = dto.nombre,
                        marca = dto.marca,
                        presentacion = dto.presentacion,
                        precioMin = dto.precioMin,
                        precioMax = dto.precioMax,
                        sucursalesDisponibles = dto.sucursalesDisponibles,
                        timestamp = System.currentTimeMillis()
                    )
                }
                preciosClarosProductDao.insertAll(entities)
            }
            return dtos
        } catch (e: Exception) {
            // Si la llamada falla (sin internet), retornar lo que haya en cache aunque esté vencido
            if (cached.isNotEmpty()) {
                return cached.map {
                    PreciosClarosProductDto(
                        id = it.apiProductId,
                        nombre = it.nombre,
                        marca = it.marca,
                        presentacion = it.presentacion,
                        precioMin = it.precioMin,
                        precioMax = it.precioMax,
                        sucursalesDisponibles = it.sucursalesDisponibles
                    )
                }
            }
            return emptyList()
        }
    }

    suspend fun seedDefaultCategories() {
        val email = getUserEmail()
        DEFAULT_CATEGORIES.forEach { name ->
            if (categoryDao.getCategoryByName(name, email) == null) {
                categoryDao.insert(CategoryEntity(name = name, userEmail = email))
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
        val categoryName = purchase.categoryId?.let { categoryMap[it]?.name } ?: CATEGORY_FALLBACK
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
            timestampMs = purchase.timestamp,
            latitude = purchase.latitude,
            longitude = purchase.longitude
        )
    }
}
