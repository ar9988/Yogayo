package com.d104.yogaapp.features.signup

sealed class SignUpIntent {
    data class UpdateId(val id: String) : SignUpIntent()
    data class UpdatePassword(val password: String) : SignUpIntent()
    data class UpdateName(val name : String) : SignUpIntent()
    data class UpdateNickname(val nickname:String) : SignUpIntent()
    data class UpdateImageUri(val uri:String) :SignUpIntent()
    data class TogglePasswordVisibility(val isVisible:Boolean) : SignUpIntent()
    data object SignUp : SignUpIntent()
    data object SignUpSuccess : SignUpIntent()
    data class SignUpFailure(val error: String) : SignUpIntent()
    data object NavigateToLogin : SignUpIntent()
}