package com.undef.prowallet.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.undef.prowallet.R
import com.undef.prowallet.data.AppRepository
import com.undef.prowallet.data.CategoryEntity
import com.undef.prowallet.data.ocr.ParsedTicket
import com.undef.prowallet.data.ocr.TicketOcrService
import com.undef.prowallet.data.ocr.TicketParser
import com.undef.prowallet.domain.Product
import com.undef.prowallet.domain.Purchase
import com.undef.prowallet.util.LocationHelper
import com.undef.prowallet.util.NotificationHelper
import com.undef.prowallet.util.SessionManager
import com.undef.prowallet.util.isCurrentMonth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class OcrStatus { Idle, Processing, Success, Error }

data class PurchaseUiState(
    val storeName: String = "",
    val date: String = SimpleDateFormat("MM/dd/yy", Locale.getDefault()).format(Date()),
    val time: String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
    val category: String = AppRepository.CATEGORY_FALLBACK,
    val categories: List<CategoryEntity> = emptyList(),
    val products: List<Product> = emptyList(),
    val currentProductCode: String = "",
    val currentProductName: String = "",
    val currentProductDescription: String = "",
    val currentProductPrice: String = "",
    val editingProductId: String? = null,
    val isSaving: Boolean = false,
    val savedSuccess: Boolean = false,
    val savedPurchaseId: String? = null,
    val saveError: Boolean = false,
    val validationError: Boolean = false,
    val productError: Boolean = false,
    val editingPurchaseId: String? = null,
    val ticketImageUri: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val isFetchingLocation: Boolean = false,
    val ocrStatus: OcrStatus = OcrStatus.Idle,
    val ocrParsedTicket: ParsedTicket? = null,
    val showOcrConfirmDialog: Boolean = false
)

class PurchaseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppRepository(application)
    private val sessionManager = SessionManager(application)

    private val _uiState = MutableStateFlow(PurchaseUiState())
    val uiState: StateFlow<PurchaseUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.categoriesFlow.collect { cats ->
                _uiState.value = _uiState.value.copy(categories = cats)
            }
        }
    }

    fun fetchLocation() {
        if (_uiState.value.isFetchingLocation) return
        _uiState.value = _uiState.value.copy(isFetchingLocation = true)
        viewModelScope.launch {
            val coords = LocationHelper(getApplication()).getLocation()
            _uiState.value = _uiState.value.copy(
                latitude = coords?.first,
                longitude = coords?.second,
                isFetchingLocation = false
            )
        }
    }

    fun onTicketImageSelected(uri: String?) { _uiState.value = _uiState.value.copy(ticketImageUri = uri) }

    fun processTicketImage(context: Context, uri: Uri) {
        _uiState.value = _uiState.value.copy(
            ocrStatus = OcrStatus.Processing,
            ocrParsedTicket = null,
            showOcrConfirmDialog = false
        )
        viewModelScope.launch {
            try {
                val rawText = TicketOcrService.recognizeText(context, uri)
                val parsed = TicketParser.parse(rawText)
                if (parsed.isEmpty) {
                    _uiState.value = _uiState.value.copy(ocrStatus = OcrStatus.Error)
                } else {
                    _uiState.value = _uiState.value.copy(
                        ocrStatus = OcrStatus.Success,
                        ocrParsedTicket = parsed,
                        showOcrConfirmDialog = true
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(ocrStatus = OcrStatus.Error)
            }
        }
    }

    fun confirmDetectedTicket() {
        val parsed = _uiState.value.ocrParsedTicket ?: return

        parsed.storeName?.let { onStoreNameChange(it) }
        parsed.date?.let { onDateChange(it) }
        parsed.time?.let { onTimeChange(it) }
        parsed.items.forEach { item ->
            onProductNameChange(item.name)
            onProductPriceChange(item.price.toString())
            addOrUpdateProduct()
        }

        _uiState.value = _uiState.value.copy(
            showOcrConfirmDialog = false,
            ocrStatus = OcrStatus.Idle,
            ocrParsedTicket = null
        )
    }

    fun dismissOcrDialog() {
        _uiState.value = _uiState.value.copy(
            showOcrConfirmDialog = false,
            ocrStatus = OcrStatus.Idle,
            ocrParsedTicket = null
        )
    }

    fun onStoreNameChange(value: String) { _uiState.value = _uiState.value.copy(storeName = value) }
    fun onDateChange(value: String) { _uiState.value = _uiState.value.copy(date = value) }
    fun onTimeChange(value: String) { _uiState.value = _uiState.value.copy(time = value) }
    fun onCategoryChange(value: String) { _uiState.value = _uiState.value.copy(category = value) }
    fun onProductCodeChange(value: String) { _uiState.value = _uiState.value.copy(currentProductCode = value) }
    fun onProductNameChange(value: String) { _uiState.value = _uiState.value.copy(currentProductName = value) }
    fun onProductDescriptionChange(value: String) { _uiState.value = _uiState.value.copy(currentProductDescription = value) }
    fun onProductPriceChange(value: String) { _uiState.value = _uiState.value.copy(currentProductPrice = value) }

    fun addOrUpdateProduct() {
        val state = _uiState.value
        val price = state.currentProductPrice.toDoubleOrNull() ?: 0.0
        if (state.currentProductName.isBlank() || price <= 0.0) {
            _uiState.value = state.copy(productError = true)
            return
        }
        _uiState.value = state.copy(productError = false)

        val resolvedCode = state.currentProductCode.trim().ifBlank { UUID.randomUUID().toString() }

        val newProducts = if (state.editingProductId != null) {
            state.products.map {
                if (it.id == state.editingProductId) it.copy(
                    code = resolvedCode,
                    name = state.currentProductName,
                    description = state.currentProductDescription,
                    price = state.currentProductPrice.toDoubleOrNull() ?: 0.0
                ) else it
            }
        } else {
            state.products + Product(
                id = "tmp_${System.currentTimeMillis()}",
                code = resolvedCode,
                name = state.currentProductName,
                description = state.currentProductDescription,
                price = state.currentProductPrice.toDoubleOrNull() ?: 0.0
            )
        }

        _uiState.value = state.copy(
            products = newProducts,
            currentProductCode = "",
            currentProductName = "",
            currentProductDescription = "",
            currentProductPrice = "",
            editingProductId = null,
            productError = false
        )
    }

    fun editProduct(product: Product) {
        _uiState.value = _uiState.value.copy(
            editingProductId = product.id,
            currentProductCode = product.code,
            currentProductName = product.name,
            currentProductDescription = product.description,
            currentProductPrice = product.price.toString()
        )
    }

    fun removeProduct(productId: String) {
        _uiState.value = _uiState.value.copy(
            products = _uiState.value.products.filter { it.id != productId }
        )
    }

    fun addCategory(name: String) {
        viewModelScope.launch { repository.addCategory(name) }
    }

    fun deleteCategory(id: Int) {
        viewModelScope.launch { repository.deleteCategory(id) }
    }

    fun updateCategoryName(id: Int, newName: String) {
        viewModelScope.launch { repository.updateCategory(id, newName) }
    }

    fun guardarCompra(compra: Purchase) {
        _uiState.value = _uiState.value.copy(isSaving = true, saveError = false)
        viewModelScope.launch {
            try {
                val id = repository.guardarCompra(compra)
                _uiState.value = _uiState.value.copy(savedSuccess = true, savedPurchaseId = id.toString())
                checkBudgetAndNotify()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(saveError = true)
            } finally {
                _uiState.value = _uiState.value.copy(isSaving = false)
            }
        }
    }

    fun savePurchase() {
        val state = _uiState.value
        if (state.isSaving) return

        if (state.storeName.isBlank()) {
            _uiState.value = state.copy(validationError = true)
            return
        }

        val totalAmount = state.products.sumOf { it.price }

        val purchase = Purchase(
            id = "",
            storeName = state.storeName,
            date = state.date,
            time = state.time,
            totalAmount = totalAmount,
            category = state.category,
            products = state.products,
            ticketImageUri = state.ticketImageUri,
            latitude = state.latitude,
            longitude = state.longitude
        )

        guardarCompra(purchase)
    }

    // Notificación real del sistema (no solo el insight in-app de NotificationsScreen):
    // se dispara una sola vez por guardado, reusando el mismo umbral de 80%/100% que ya
    // calculaba NotificationsViewModel, para no duplicar la lógica de negocio.
    private suspend fun checkBudgetAndNotify() {
        if (!sessionManager.notificationsEnabled.first()) return
        val budget = sessionManager.monthlyBudget.first()
        if (budget <= 0) return

        val spent = repository.purchasesFlow.first().filter { it.isCurrentMonth() }.sumOf { it.totalAmount }
        val percent = ((spent / budget) * 100).toInt()
        val context = getApplication<Application>()
        val title = context.getString(R.string.settings_budget_alerts_title)

        when {
            spent > budget -> NotificationHelper.showBudgetAlert(
                context, title,
                context.getString(R.string.notif_budget_over_format, "%.0f".format(spent - budget), percent)
            )
            percent >= 80 -> NotificationHelper.showBudgetAlert(
                context, title,
                context.getString(R.string.notif_budget_near_format, percent, "%.0f".format(spent), "%.0f".format(budget))
            )
        }
    }

    fun clearSavedSuccess() { _uiState.value = _uiState.value.copy(savedSuccess = false) }
    fun clearSaveError() { _uiState.value = _uiState.value.copy(saveError = false) }
    fun clearValidationError() { _uiState.value = _uiState.value.copy(validationError = false) }

    fun loadForEdit(purchaseId: String) {
        viewModelScope.launch {
            val purchase = repository.getPurchaseById(purchaseId.toInt()) ?: return@launch
            _uiState.value = _uiState.value.copy(
                editingPurchaseId = purchaseId,
                storeName = purchase.storeName,
                date = purchase.date,
                time = purchase.time,
                category = purchase.category,
                products = purchase.products,
                ticketImageUri = purchase.ticketImageUri,
                latitude = purchase.latitude,
                longitude = purchase.longitude,
                currentProductCode = "",
                currentProductName = "",
                currentProductDescription = "",
                currentProductPrice = "",
                editingProductId = null,
                isSaving = false,
                savedSuccess = false,
                savedPurchaseId = null,
                saveError = false,
                validationError = false
            )
        }
    }

    fun updatePurchase() {
        val state = _uiState.value
        val id = state.editingPurchaseId?.toIntOrNull() ?: return
        if (state.isSaving) return

        if (state.storeName.isBlank()) {
            _uiState.value = state.copy(validationError = true)
            return
        }

        val totalAmount = state.products.sumOf { it.price }
        val purchase = Purchase(
            id = id.toString(),
            storeName = state.storeName,
            date = state.date,
            time = state.time,
            totalAmount = totalAmount,
            category = state.category,
            products = state.products,
            ticketImageUri = state.ticketImageUri,
            latitude = state.latitude,
            longitude = state.longitude
        )

        _uiState.value = state.copy(isSaving = true, saveError = false)
        viewModelScope.launch {
            try {
                repository.updatePurchase(id, purchase)
                _uiState.value = _uiState.value.copy(savedSuccess = true, savedPurchaseId = id.toString())
                checkBudgetAndNotify()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(saveError = true)
            } finally {
                _uiState.value = _uiState.value.copy(isSaving = false)
            }
        }
    }

    fun resetForm() {
        _uiState.value = _uiState.value.copy(
            storeName = "",
            date = SimpleDateFormat("MM/dd/yy", Locale.getDefault()).format(Date()),
            time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
            category = AppRepository.CATEGORY_FALLBACK,
            products = emptyList(),
            currentProductCode = "",
            currentProductName = "",
            currentProductDescription = "",
            currentProductPrice = "",
            editingProductId = null,
            editingPurchaseId = null,
            savedPurchaseId = null,
            ticketImageUri = null,
            latitude = null,
            longitude = null,
            isFetchingLocation = false,
            isSaving = false,
            savedSuccess = false,
            saveError = false,
            validationError = false,
            productError = false,
            ocrStatus = OcrStatus.Idle,
            ocrParsedTicket = null,
            showOcrConfirmDialog = false
        )
    }
}
