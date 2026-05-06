package com.undef.prowallet.viewmodel

import androidx.lifecycle.ViewModel
import com.undef.prowallet.data.MockRepository
import com.undef.prowallet.domain.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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

class AuthViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        // Mock: always succeed
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            isLoggedIn = true,
            user = MockRepository.currentUser
        )
    }

    fun register(fullName: String, email: String, password: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        // Mock: always succeed
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            registrationSuccess = true,
            user = MockRepository.currentUser.copy(fullName = fullName, email = email)
        )
    }

    fun logout() {
        _uiState.value = AuthUiState()
    }

    fun clearRegistrationSuccess() {
        _uiState.value = _uiState.value.copy(registrationSuccess = false)
    }

    fun sendResetCode(email: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        // Mock: success
        _uiState.value = _uiState.value.copy(isLoading = false, resetEmailSent = true)
    }

    fun verifyCode(code: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        // Mock: success
        _uiState.value = _uiState.value.copy(isLoading = false, codeVerified = true)
    }

    fun updatePassword(password: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        // Mock: success
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
