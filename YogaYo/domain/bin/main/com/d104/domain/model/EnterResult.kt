package com.d104.domain.model

sealed class EnterResult {
    data object Success : EnterResult()
    sealed class Error : EnterResult() {
        data class BadRequest(val message: String) : Error()
        data class Unauthorized(val message: String) : Error()
    }
}