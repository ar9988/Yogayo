package com.d104.data.remote.dto

data class RoomResponseDto(
    val roomId : Long,
    val userId : Long,
    val userNickname: String,
    val roomMax : Int,
    val roomCount : Int,
    val roomName : String,
    val hasPassword : Boolean,
    val pose:List<YogaPoseDto>
)