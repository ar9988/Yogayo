package com.d104.data.remote.dto

import com.d104.domain.model.YogaPoseWithOrder

data class CreateRoomRequestDto(
    val roomName: String,
    val roomMax: Int,
    val hasPassword: Boolean,
    val password: String,
    val pose: List<YogaPoseWithOrder>
)