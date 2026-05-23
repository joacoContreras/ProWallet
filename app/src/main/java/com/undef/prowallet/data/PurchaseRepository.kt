package com.undef.prowallet.data

import com.undef.prowallet.domain.Purchase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object PurchaseRepository {

    private val _purchases = MutableStateFlow<List<Purchase>>(MockRepository.mockPurchases)
    val purchases: StateFlow<List<Purchase>> = _purchases.asStateFlow()

    fun addPurchase(purchase: Purchase) {
        _purchases.value = listOf(purchase) + _purchases.value
    }

    fun deletePurchase(id: String) {
        _purchases.value = _purchases.value.filter { it.id != id }
    }

    fun getPurchaseById(id: String): Purchase? =
        _purchases.value.find { it.id == id }
}
