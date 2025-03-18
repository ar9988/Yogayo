package com.d104.data.remote.dto

data class SignUpResponseDto(
    val success: Boolean,
    val status: Int? = null,
    val message: String? = null,
    val error: String? = null
)