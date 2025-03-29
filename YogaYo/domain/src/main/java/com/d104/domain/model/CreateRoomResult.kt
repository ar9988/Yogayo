package com.d104.domain.model

sealed class CreateRoomResult {
    data class Success(
        val room: Room
    ) : CreateRoomResult()
    sealed class Error : CreateRoomResult() {
        data class BadRequest(val message: String) : Error()
        data class Unauthorized(val message: String) : Error()
    }
}