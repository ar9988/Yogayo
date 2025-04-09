package com.d104.data.remote.dto

import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class MultiBestPhotoResponseDto(
    val poseName:String,
    val poseUrl:String,
    val room_order_index:Int,
)