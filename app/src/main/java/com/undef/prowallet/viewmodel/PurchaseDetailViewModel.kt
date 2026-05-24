package com.undef.prowallet.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.undef.prowallet.data.AppRepository
import com.undef.prowallet.domain.Purchase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PurchaseDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppRepository(application)

    private val _purchase = MutableStateFlow<Purchase?>(null)
    val purchase: StateFlow<Purchase?> = _purchase.asStateFlow()

    fun loadPurchase(id: String) {
        viewModelScope.launch {
            _purchase.value = repository.getPurchaseById(id.toIntOrNull() ?: return@launch)
        }
    }

    fun deletePurchase(id: String, onDone: () -> Unit) {
        viewModelScope.launch {
            repository.deletePurchase(id.toIntOrNull() ?: return@launch)
            onDone()
        }
    }
}
