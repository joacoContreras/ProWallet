package com.undef.prowallet.viewmodel

import androidx.lifecycle.ViewModel
import com.undef.prowallet.data.PurchaseRepository
import com.undef.prowallet.domain.Purchase

class PurchaseDetailViewModel : ViewModel() {

    fun getPurchaseById(id: String): Purchase? =
        PurchaseRepository.getPurchaseById(id)

    fun deletePurchase(id: String) {
        PurchaseRepository.deletePurchase(id)
    }
}
