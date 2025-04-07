package com.d104.data.remote.dto

sealed class SignUpResponseDto {
    data class Success(
        val success: Boolean = true
    ) : SignUpResponseDto()

    data class Error(
        val success: Boolean = false,
        val status: Int,
        val message: String?,
        val error: String?
    ) : SignUpResponseDto()
}