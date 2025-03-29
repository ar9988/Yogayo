package com.d104.data.remote.dto

import com.d104.domain.model.UserCourse
import com.d104.domain.model.YogaPoseWithOrder

data class CreateRoomRequestDto(
    val roomName: String,
    val roomMax: Int,
    val isPassword: Boolean,
    val password: String,
    val courseName: String,
    val userCourse: UserCourse
)