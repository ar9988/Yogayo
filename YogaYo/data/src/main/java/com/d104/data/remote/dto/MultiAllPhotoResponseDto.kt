package com.d104.data.remote.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MultiAllPhotoResponseDto(
    val poseUrl: String,
    val accuracy: Float,
    val poseTime:Float,
    val ranking: Int,
    val userName: String
)