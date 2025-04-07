package com.d104.data.remote.dto

sealed class EnterRoomResponseDto {
    data class Success(
        val success: Boolean = true
    ) : EnterRoomResponseDto()

    data class Error(
        val success: Boolean = false,
        val status: Int,
        val message: String?,
        val error: String?
    ) : EnterRoomResponseDto()
}