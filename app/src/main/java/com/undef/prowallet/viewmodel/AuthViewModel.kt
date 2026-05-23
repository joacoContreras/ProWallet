package com.undef.prowallet.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.undef.prowallet.data.MockRepository
import com.undef.prowallet.domain.User
import com.undef.prowallet.util.SessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val user: User? = null,
    val error: String? = null,
    val registrationSuccess: Boolean = false,
    val resetEmailSent: Boolean = false,
    val codeVerified: Boolean = false,
    val passwordUpdated: Boolean = false
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application)

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val isLoggedIn: Flow<Boolean> = sessionManager.isLoggedIn

    fun login(email: String, password: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            sessionManager.saveSession(email)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                isLoggedIn = true,
                user = MockRepository.currentUser
            )
        }
    }

    fun register(fullName: String, email: String, password: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            sessionManager.saveSession(email)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                registrationSuccess = true,
                user = MockRepository.currentUser.copy(fullName = fullName, email = email)
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            sessionManager.clearSession()
            _uiState.value = AuthUiState()
        }
    }

    fun clearRegistrationSuccess() {
        _uiState.value = _uiState.value.copy(registrationSuccess = false)
    }

    fun sendResetCode(email: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        _uiState.value = _uiState.value.copy(isLoading = false, resetEmailSent = true)
    }

    fun verifyCode(code: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        _uiState.value = _uiState.value.copy(isLoading = false, codeVerified = true)
    }

    fun updatePassword(password: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        _uiState.value = _uiState.value.copy(isLoading = false, passwordUpdated = true)
    }

    fun resetFlow() {
        _uiState.value = _uiState.value.copy(
            resetEmailSent = false,
            codeVerified = false,
            passwordUpdated = false,
            error = null
        )
    }
}
