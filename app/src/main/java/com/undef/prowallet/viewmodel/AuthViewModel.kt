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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val user: User? = null,
    val error: AuthError? = null,
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
            _uiState.value = _uiState.value.copy(error = AuthError.EmptyFields)
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            _uiState.value = _uiState.value.copy(error = AuthError.InvalidEmail)
            return
        }
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val user = userDao.getUserByEmail(trimmedEmail)
            when {
                user == null -> _uiState.value = _uiState.value.copy(isLoading = false, error = AuthError.EmailNotFound)
                !withContext(Dispatchers.Default) { verifyPassword(password, user.password) } ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = AuthError.WrongPassword)
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
                _uiState.value = _uiState.value.copy(error = AuthError.EmptyFields)
                return
            }
            !Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches() -> {
                _uiState.value = _uiState.value.copy(error = AuthError.InvalidEmail)
                return
            }
            password.length < 8 -> {
                _uiState.value = _uiState.value.copy(error = AuthError.PasswordTooShort)
                return
            }
            password != confirmPassword -> {
                _uiState.value = _uiState.value.copy(error = AuthError.PasswordMismatch)
                return
            }
        }

        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            if (userDao.getUserByEmail(trimmedEmail) != null) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = AuthError.EmailAlreadyExists)
                return@launch
            }
            val nameParts = trimmedName.split(" ", limit = 2)
            val hashed = withContext(Dispatchers.Default) { hashPassword(password) }
            userDao.insert(
                UserEntity(
                    name = nameParts[0],
                    lastname = nameParts.getOrElse(1) { "" },
                    email = trimmedEmail,
                    password = hashed
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
            _uiState.value = _uiState.value.copy(error = AuthError.EnterEmail)
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            _uiState.value = _uiState.value.copy(error = AuthError.InvalidEmail)
            return
        }
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            if (userDao.getUserByEmail(trimmedEmail) == null) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = AuthError.EmailNotFound)
                return@launch
            }
            pendingEmail = trimmedEmail
            _uiState.value = _uiState.value.copy(isLoading = false, resetEmailSent = true)
        }
    }

    fun updatePassword(password: String) {
        if (!_uiState.value.codeVerified) return
        val email = pendingEmail ?: return
        if (password.length < 8) {
            _uiState.value = _uiState.value.copy(error = AuthError.PasswordTooShort)
            return
        }
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val user = userDao.getUserByEmail(email) ?: return@launch
            val hashed = withContext(Dispatchers.Default) { hashPassword(password) }
            userDao.update(user.copy(password = hashed))
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
        if (code.length != 6 || !code.all { it.isDigit() }) {
            _uiState.value = _uiState.value.copy(error = AuthError.InvalidCode)
            return
        }
        _uiState.value = _uiState.value.copy(codeVerified = true)
    }

    fun resendCode() {
        val email = pendingEmail ?: return
        _uiState.value = _uiState.value.copy(codeVerified = false, error = null)
        sendResetCode(email)
    }

    fun resetFlow() {
        _uiState.value = _uiState.value.copy(
            resetEmailSent = false,
            codeVerified = false,
            passwordUpdated = false,
            error = null
        )
    }

    private fun hashPassword(password: String): String {
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val spec = PBEKeySpec(password.toCharArray(), salt, 65_536, 256)
        val hash = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        spec.clearPassword()
        val enc = Base64.getEncoder()
        return "${enc.encodeToString(salt)}:${enc.encodeToString(hash)}"
    }

    private fun verifyPassword(password: String, stored: String): Boolean {
        val parts = stored.split(":")
        if (parts.size != 2) return false
        val dec = Base64.getDecoder()
        val salt = dec.decode(parts[0])
        val expectedHash = dec.decode(parts[1])
        val spec = PBEKeySpec(password.toCharArray(), salt, 65_536, 256)
        val actualHash = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        spec.clearPassword()
        return MessageDigest.isEqual(expectedHash, actualHash)
    }

    private fun UserEntity.toDomain() = User(
        id = id.toString(),
        fullName = "$name $lastname".trim(),
        email = email,
        avatarUrl = avatarUrl ?: ""
    )
}
