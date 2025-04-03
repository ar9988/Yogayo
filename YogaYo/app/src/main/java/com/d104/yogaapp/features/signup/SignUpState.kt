package com.d104.yogaapp.features.signup

data class SignUpState (
    val id: String = "",
    val password: String = "",
    val name: String ="",
    val nickname: String ="",
    val uri:String="",
    val isIdChecked: Boolean = false,
    val passwordIsPossible: Boolean = false,
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSignUpSuccessful: Boolean = false
)