package com.d104.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class BadgeResponseDto(
    @Json(name = "badgeId")
    val badgeId: Long,

    @Json(name = "badgeName")
    val badgeName: String,

    @Json(name = "badgeProgress")
    val badgeProgress: Int,

    @Json(name = "highLevel")
    val highLevel: Int,

    @Json(name = "badgeDetails")
    val badgeDetails: List<BadgeDetailResponseDto>
)