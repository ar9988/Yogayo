package com.d104.data.remote.dto

import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class MultiBestPhotoResponseDto(
    val url:String
)