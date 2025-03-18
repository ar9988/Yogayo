package com.d104.domain.model

sealed class SignUpResult {
    data object Success : SignUpResult()
    sealed class Error : SignUpResult() {
        data class BadRequest(val message: String) : Error()
        data class ConflictUser(val message: String) : Error()
    }
}