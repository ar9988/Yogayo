package com.d104.yogaapp.features.login

sealed class LoginIntent {
    data class UpdateId(val id: String) : LoginIntent()
    data class UpdatePassword(val password: String) : LoginIntent()
    data class TogglePasswordVisibility(val isVisible: Boolean) : LoginIntent()
    data object Login : LoginIntent()
    data object LoginSuccess : LoginIntent()
    data class LoginFailure(val error: String) : LoginIntent()
    data object NavigateToSignUp : LoginIntent()
}
