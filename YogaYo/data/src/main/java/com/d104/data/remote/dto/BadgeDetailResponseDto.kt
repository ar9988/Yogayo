package com.d104.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BadgeDetailResponseDto(
    @Json(name = "badgeDetailId")
    val badgeDetailId: Long,

    @Json(name = "badgeDetailName")
    val badgeDetailName: String,

    @Json(name = "badgeDetailImg")
    val badgeDetailImg: String,

    @Json(name = "badgeDescription")
    val badgeDescription: String,

    @Json(name = "badgeGoal")
    val badgeGoal: Int,

    @Json(name = "badgeLevel")
    val badgeLevel: Int
)


