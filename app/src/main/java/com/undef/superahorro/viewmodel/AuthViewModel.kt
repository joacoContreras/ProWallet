package com.undef.superahorro.viewmodel

import androidx.lifecycle.ViewModel
import com.undef.superahorro.data.MockRepository
import com.undef.superahorro.domain.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val user: User? = null,
    val error: String? = null,
    val registrationSuccess: Boolean = false
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
}
