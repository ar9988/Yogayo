package com.d104.domain.model

data class Room(
    val roomId : Long,
    val userId: Long,
    val userNickname: String,
    val roomMax: Int,
    val roomCount: Int,
    val roomName: String,
    val isPassword: Boolean,
    val userCourse: UserCourse
)