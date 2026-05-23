package com.undef.prowallet.viewmodel

import android.app.Application
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.undef.prowallet.data.ProWalletDatabase
import com.undef.prowallet.data.UserEntity
import com.undef.prowallet.data.dao.UserDao
import com.undef.prowallet.domain.User
import com.undef.prowallet.util.SessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.security.MessageDigest

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
    private val userDao: UserDao = ProWalletDatabase.getInstance(application).userDao()

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val isLoggedIn: Flow<Boolean> = sessionManager.isLoggedIn

    private var pendingEmail: String? = null

    fun login(email: String, password: String) {
        val trimmedEmail = email.trim().lowercase()
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Completá todos los campos")
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            _uiState.value = _uiState.value.copy(error = "Email inválido")
            return
        }
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val user = userDao.getUserByEmail(trimmedEmail)
            when {
                user == null -> _uiState.value = _uiState.value.copy(isLoading = false, error = "No existe una cuenta con ese email")
                user.password != hashPassword(password) -> _uiState.value = _uiState.value.copy(isLoading = false, error = "Contraseña incorrecta")
                else -> {
                    sessionManager.saveSession(user.email)
                    _uiState.value = _uiState.value.copy(isLoading = false, isLoggedIn = true, user = user.toDomain())
                }
            }
        }
    }

    fun register(fullName: String, email: String, password: String, confirmPassword: String) {
        val trimmedEmail = email.trim().lowercase()
        val trimmedName = fullName.trim()
        when {
            trimmedName.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank() -> {
                _uiState.value = _uiState.value.copy(error = "Completá todos los campos")
                return
            }
            !Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches() -> {
                _uiState.value = _uiState.value.copy(error = "Email inválido")
                return
            }
            password.length < 6 -> {
                _uiState.value = _uiState.value.copy(error = "La contraseña debe tener al menos 6 caracteres")
                return
            }
            password != confirmPassword -> {
                _uiState.value = _uiState.value.copy(error = "Las contraseñas no coinciden")
                return
            }
        }

        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            if (userDao.getUserByEmail(trimmedEmail) != null) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Ya existe una cuenta con ese email")
                return@launch
            }
            val nameParts = trimmedName.split(" ", limit = 2)
            userDao.insert(
                UserEntity(
                    name = nameParts[0],
                    lastname = nameParts.getOrElse(1) { "" },
                    email = trimmedEmail,
                    password = hashPassword(password)
                )
            )
            sessionManager.saveSession(trimmedEmail)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                registrationSuccess = true,
                user = User(id = trimmedEmail, fullName = trimmedName, email = trimmedEmail)
            )
        }
    }

    fun sendResetCode(email: String) {
        val trimmedEmail = email.trim().lowercase()
        if (trimmedEmail.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Ingresá tu email")
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            _uiState.value = _uiState.value.copy(error = "Email inválido")
            return
        }
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            if (userDao.getUserByEmail(trimmedEmail) == null) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "No existe una cuenta con ese email")
                return@launch
            }
            pendingEmail = trimmedEmail
            _uiState.value = _uiState.value.copy(isLoading = false, resetEmailSent = true)
        }
    }

    fun updatePassword(password: String) {
        val email = pendingEmail ?: return
        if (password.length < 6) {
            _uiState.value = _uiState.value.copy(error = "La contraseña debe tener al menos 6 caracteres")
            return
        }
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val user = userDao.getUserByEmail(email) ?: return@launch
            userDao.update(user.copy(password = hashPassword(password)))
            pendingEmail = null
            _uiState.value = _uiState.value.copy(isLoading = false, passwordUpdated = true)
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

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun verifyCode(code: String) {
        _uiState.value = _uiState.value.copy(codeVerified = true)
    }

    fun resendCode() { }

    fun resetFlow() {
        _uiState.value = _uiState.value.copy(
            resetEmailSent = false,
            codeVerified = false,
            passwordUpdated = false,
            error = null
        )
    }

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun UserEntity.toDomain() = User(
        id = id.toString(),
        fullName = "$name $lastname".trim(),
        email = email,
        avatarUrl = avatarUrl ?: ""
    )
}
