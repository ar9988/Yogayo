package com.d104.yogaapp.features.login

import javax.inject.Inject

class LoginReducer  @Inject constructor(){
    fun reduce(currentState: LoginState, intent: LoginIntent): LoginState {
        return when (intent) {
            is LoginIntent.UpdateId -> currentState.copy(
                id = intent.id,
                errorMessage = null
            )
            is LoginIntent.UpdatePassword -> currentState.copy(
                password = intent.password,
                errorMessage = null
            )
            is LoginIntent.TogglePasswordVisibility -> currentState.copy(
                isPasswordVisible = intent.isVisible
            )
            is LoginIntent.Login -> currentState.copy(
                isLoading = true)
            is LoginIntent.LoginSuccess -> currentState.copy(
                isLoading = false,
                isLoginSuccessful = true
            )
            is LoginIntent.LoginFailure -> currentState.copy(
                isLoading = false,
                errorMessage = intent.error
            )
        }
    }
}