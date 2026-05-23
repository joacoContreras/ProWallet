package com.undef.prowallet.viewmodel

import androidx.lifecycle.ViewModel
import com.undef.prowallet.data.PurchaseRepository
import com.undef.prowallet.domain.Product
import com.undef.prowallet.domain.Purchase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class PurchaseUiState(
    val storeName: String = "",
    val date: String = "",
    val time: String = "",
    val totalAmount: String = "",
    val products: List<Product> = emptyList(),
    val currentProductCode: String = "10492",
    val currentProductName: String = "",
    val currentProductDescription: String = "",
    val currentProductPrice: String = "0.00",
    val editingProductId: String? = null,
    val savedSuccess: Boolean = false
)

class PurchaseViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(PurchaseUiState())
    val uiState: StateFlow<PurchaseUiState> = _uiState.asStateFlow()

    fun onStoreNameChange(value: String) {
        _uiState.value = _uiState.value.copy(storeName = value)
    }

    fun onDateChange(value: String) {
        _uiState.value = _uiState.value.copy(date = value)
    }

    fun onTimeChange(value: String) {
        _uiState.value = _uiState.value.copy(time = value)
    }

    fun onTotalAmountChange(value: String) {
        _uiState.value = _uiState.value.copy(totalAmount = value)
    }

    fun onProductCodeChange(value: String) {
        _uiState.value = _uiState.value.copy(currentProductCode = value)
    }

    fun onProductNameChange(value: String) {
        _uiState.value = _uiState.value.copy(currentProductName = value)
    }

    fun onProductDescriptionChange(value: String) {
        _uiState.value = _uiState.value.copy(currentProductDescription = value)
    }

    fun onProductPriceChange(value: String) {
        _uiState.value = _uiState.value.copy(currentProductPrice = value)
    }

    fun addOrUpdateProduct() {
        val state = _uiState.value
        if (state.currentProductName.isBlank()) return

        val newProducts = if (state.editingProductId != null) {
            state.products.map {
                if (it.id == state.editingProductId) {
                    it.copy(
                        code = state.currentProductCode,
                        name = state.currentProductName,
                        description = state.currentProductDescription,
                        price = state.currentProductPrice.toDoubleOrNull() ?: 0.0
                    )
                } else it
            }
        } else {
            val product = Product(
                id = "p_${System.currentTimeMillis()}",
                code = state.currentProductCode,
                name = state.currentProductName,
                description = state.currentProductDescription,
                price = state.currentProductPrice.toDoubleOrNull() ?: 0.0
            )
            state.products + product
        }

        _uiState.value = state.copy(
            products = newProducts,
            currentProductName = "",
            currentProductDescription = "",
            currentProductPrice = "0.00",
            currentProductCode = "10492",
            editingProductId = null
        )
    }

    fun editProduct(product: Product) {
        _uiState.value = _uiState.value.copy(
            editingProductId = product.id,
            currentProductName = product.name,
            currentProductDescription = product.description,
            currentProductPrice = product.price.toString(),
            currentProductCode = product.code
        )
    }

    fun removeProduct(productId: String) {
        _uiState.value = _uiState.value.copy(
            products = _uiState.value.products.filter { it.id != productId }
        )
    }

    fun savePurchase() {
        val state = _uiState.value
        val purchase = Purchase(
            id = "pur_${System.currentTimeMillis()}",
            storeName = state.storeName,
            date = state.date,
            time = state.time,
            totalAmount = state.totalAmount.toDoubleOrNull() ?: state.products.sumOf { it.price },
            category = "Other",
            products = state.products
        )
        PurchaseRepository.addPurchase(purchase)
        _uiState.value = _uiState.value.copy(savedSuccess = true)
    }

    fun clearSavedSuccess() {
        _uiState.value = _uiState.value.copy(savedSuccess = false)
    }

    fun resetForm() {
        _uiState.value = PurchaseUiState()
    }
}
