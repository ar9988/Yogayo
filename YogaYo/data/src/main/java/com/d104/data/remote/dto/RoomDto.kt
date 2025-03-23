package com.d104.data.remote.dto

import com.d104.domain.model.YogaPose

data class RoomDto(
    val roomId : Int,
    val userNickname : String,
    val roomMax : Int,
    val roomCount : Int,
    val roomName : String,
    val isPassword : Boolean,
    val poses : List<YogaPose>
)