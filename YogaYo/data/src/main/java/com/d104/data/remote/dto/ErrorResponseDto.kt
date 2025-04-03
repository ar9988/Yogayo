package com.d104.data.remote.dto

data class ErrorResponseDto(
    val status: Int,
    val message: String,
    val error: String
)