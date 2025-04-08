package com.d104.data.remote.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MultiAllPhotoResponseDto(
    val url: String,
    val accuracy: Float,
    val time:Float,
    val ranking: Int,
)