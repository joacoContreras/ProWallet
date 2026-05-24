package com.undef.prowallet.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.undef.prowallet.data.AppRepository
import com.undef.prowallet.domain.Product
import com.undef.prowallet.domain.Purchase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class PurchaseUiState(
    val storeName: String = "",
    val date: String = SimpleDateFormat("MM/dd/yy", Locale.getDefault()).format(Date()),
    val time: String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
    val totalAmount: String = "",
    val category: String = "Other",
    val products: List<Product> = emptyList(),
    val currentProductCode: String = "",
    val currentProductName: String = "",
    val currentProductDescription: String = "",
    val currentProductPrice: String = "",
    val editingProductId: String? = null,
    val isSaving: Boolean = false,
    val savedSuccess: Boolean = false,
    val saveError: Boolean = false
)

val CATEGORIES = listOf("Groceries", "Transport", "Dining", "Coffee", "Other")

class PurchaseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppRepository(application)

    private val _uiState = MutableStateFlow(PurchaseUiState())
    val uiState: StateFlow<PurchaseUiState> = _uiState.asStateFlow()

    fun onStoreNameChange(value: String) { _uiState.value = _uiState.value.copy(storeName = value) }
    fun onDateChange(value: String) { _uiState.value = _uiState.value.copy(date = value) }
    fun onTimeChange(value: String) { _uiState.value = _uiState.value.copy(time = value) }
    fun onTotalAmountChange(value: String) { _uiState.value = _uiState.value.copy(totalAmount = value) }
    fun onCategoryChange(value: String) { _uiState.value = _uiState.value.copy(category = value) }
    fun onProductCodeChange(value: String) { _uiState.value = _uiState.value.copy(currentProductCode = value) }
    fun onProductNameChange(value: String) { _uiState.value = _uiState.value.copy(currentProductName = value) }
    fun onProductDescriptionChange(value: String) { _uiState.value = _uiState.value.copy(currentProductDescription = value) }
    fun onProductPriceChange(value: String) { _uiState.value = _uiState.value.copy(currentProductPrice = value) }

    fun addOrUpdateProduct() {
        val state = _uiState.value
        if (state.currentProductName.isBlank()) return

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
            editingProductId = null
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

    fun savePurchase() {
        val state = _uiState.value
        if (state.storeName.isBlank() || state.isSaving) return

        val totalAmount = state.totalAmount.toDoubleOrNull()
            ?: state.products.sumOf { it.price }

        val purchase = Purchase(
            id = "",
            storeName = state.storeName,
            date = state.date,
            time = state.time,
            totalAmount = totalAmount,
            category = state.category,
            products = state.products
        )

        _uiState.value = state.copy(isSaving = true, saveError = false)
        viewModelScope.launch {
            try {
                repository.savePurchase(purchase)
                _uiState.value = _uiState.value.copy(savedSuccess = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(saveError = true)
            } finally {
                _uiState.value = _uiState.value.copy(isSaving = false)
            }
        }
    }

    fun clearSavedSuccess() { _uiState.value = _uiState.value.copy(savedSuccess = false) }

    fun clearSaveError() { _uiState.value = _uiState.value.copy(saveError = false) }

    fun resetForm() { _uiState.value = PurchaseUiState() }
}
