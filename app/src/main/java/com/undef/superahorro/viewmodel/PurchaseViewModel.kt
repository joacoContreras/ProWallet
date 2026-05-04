package com.undef.superahorro.viewmodel

import androidx.lifecycle.ViewModel
import com.undef.superahorro.data.MockRepository
import com.undef.superahorro.domain.Product
import com.undef.superahorro.domain.Purchase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class PurchaseUiState(
    val storeName: String = "",
    val date: String = "",
    val totalAmount: String = "",
    val products: List<Product> = emptyList(),
    val currentProductCode: String = "10492",
    val currentProductName: String = "",
    val currentProductPrice: String = "0.00",
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

    fun onTotalAmountChange(value: String) {
        _uiState.value = _uiState.value.copy(totalAmount = value)
    }

    fun onProductCodeChange(value: String) {
        _uiState.value = _uiState.value.copy(currentProductCode = value)
    }

    fun onProductNameChange(value: String) {
        _uiState.value = _uiState.value.copy(currentProductName = value)
    }

    fun onProductPriceChange(value: String) {
        _uiState.value = _uiState.value.copy(currentProductPrice = value)
    }

    fun addProduct() {
        val state = _uiState.value
        if (state.currentProductName.isBlank()) return
        val product = Product(
            id = "p_${System.currentTimeMillis()}",
            code = state.currentProductCode,
            name = state.currentProductName,
            price = state.currentProductPrice.toDoubleOrNull() ?: 0.0
        )
        _uiState.value = state.copy(
            products = state.products + product,
            currentProductName = "",
            currentProductPrice = "0.00"
        )
    }

    fun savePurchase() {
        _uiState.value = _uiState.value.copy(savedSuccess = true)
    }

    fun resetForm() {
        _uiState.value = PurchaseUiState()
    }

    fun getPurchaseById(id: String): Purchase? =
        MockRepository.mockPurchases.find { it.id == id }
}
