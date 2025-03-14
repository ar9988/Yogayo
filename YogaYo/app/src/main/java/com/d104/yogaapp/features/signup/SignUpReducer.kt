package com.d104.yogaapp.features.signup

import javax.inject.Inject

class SignUpReducer @Inject constructor(){
    fun reduce(currentState: SignUpState, intent: SignUpIntent) : SignUpState {
        return when(intent){
            SignUpIntent.NavigateToLogin -> currentState
            SignUpIntent.SignUp -> currentState.copy(isLoading = true)
            is SignUpIntent.SignUpFailure -> currentState.copy(
                isLoading = false,
                errorMessage = intent.error
            )
            SignUpIntent.SignUpSuccess -> currentState.copy(
                isLoading = false,
                isSignUpSuccessful = true
            )
            is SignUpIntent.TogglePasswordVisibility -> currentState.copy(
                isPasswordVisible = intent.isVisible
            )
            is SignUpIntent.UpdateId -> currentState.copy(
                id = intent.id,
                errorMessage = null
            )
            is SignUpIntent.UpdatePassword -> currentState.copy(
                password = intent.password,
                errorMessage = null
            )
        }
    }
}