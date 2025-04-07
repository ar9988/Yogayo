package com.d104.data.remote.dto

import com.d104.domain.model.UserCourse

data class RoomDto(
    val roomId : Long,
    val userNickname : String,
    val roomMax : Int,
    val roomCount : Int,
    val roomName : String,
    val isPassword : Boolean,
    val userCourse : UserCourse
)