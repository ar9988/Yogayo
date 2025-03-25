package com.d104.domain.model

data class Room(
    val roomId : Int,
    val userNickName: String,
    val roomMax: Int,
    val roomCount: Int,
    val roomName: String,
    val isPassword: Boolean,
    val course: UserCourse
)