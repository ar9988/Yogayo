package com.d104.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class BestPoseHistoryResponseDto (
    @Json(name = "poseId")
    val poseId:Long,

    @Json(name = "poseName")
    val poseName:String,

    @Json(name = "poseImg")
    val poseImg: String,

    @Json(name = "bestAccuracy")
    val bestAccuracy:Float,

    @Json(name = "bestTime")
    val bestTime:Float
)