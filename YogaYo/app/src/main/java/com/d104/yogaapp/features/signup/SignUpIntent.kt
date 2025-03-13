package com.d104.yogaapp.features.signup

sealed class SignUpIntent {
    data class UpdateId(val id: String) : SignUpIntent()
    data class UpdatePassword(val password: String) : SignUpIntent()
    data class TogglePasswordVisibility(val isVisible:Boolean) : SignUpIntent()
    data object SignUp : SignUpIntent()
    data object SignUpSuccess : SignUpIntent()
    data class SignUpFailure(val error: String) : SignUpIntent()
    data object NavigateToLogin : SignUpIntent()
}