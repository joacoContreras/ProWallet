package com.undef.prowallet.viewmodel

import androidx.annotation.StringRes
import com.undef.prowallet.R

sealed class AuthError(@StringRes val messageRes: Int) {
    object EmptyFields        : AuthError(R.string.error_empty_fields)
    object InvalidEmail       : AuthError(R.string.error_invalid_email)
    object EmailNotFound      : AuthError(R.string.error_email_not_found)
    object WrongPassword      : AuthError(R.string.error_wrong_password)
    object EmailAlreadyExists : AuthError(R.string.error_email_already_exists)
    object PasswordTooShort   : AuthError(R.string.error_password_too_short)
    object PasswordMismatch   : AuthError(R.string.error_password_mismatch)
    object EnterEmail         : AuthError(R.string.error_enter_email)
}
