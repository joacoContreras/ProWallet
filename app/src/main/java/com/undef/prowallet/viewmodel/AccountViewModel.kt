package com.undef.prowallet.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.undef.prowallet.data.AccountEntity
import com.undef.prowallet.data.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AccountUiState(
    val accounts: List<AccountEntity> = emptyList()
)

class AccountViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppRepository(application)

    private val _uiState = MutableStateFlow(AccountUiState())
    val uiState: StateFlow<AccountUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.accountsFlow.collect { accounts ->
                _uiState.value = AccountUiState(accounts = accounts)
            }
        }
    }

    fun addAccount(name: String, type: String, lastFour: String, isPrimary: Boolean) {
        viewModelScope.launch { repository.addAccount(name, type, lastFour, isPrimary) }
    }

    fun updateAccount(id: Int, name: String, type: String, lastFour: String, isPrimary: Boolean) {
        viewModelScope.launch { repository.updateAccount(id, name, type, lastFour, isPrimary) }
    }

    fun deleteAccount(id: Int) {
        viewModelScope.launch { repository.deleteAccount(id) }
    }
}
