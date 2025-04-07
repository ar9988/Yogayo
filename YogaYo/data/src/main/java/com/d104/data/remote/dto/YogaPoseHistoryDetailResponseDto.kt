package com.d104.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class YogaPoseHistoryDetailResponseDto (
    @Json(name = "poseId")
    val poseId: Long,

    @Json(name = "poseName")
    val poseName:String,

    @Json(name = "poseImg")
    val poseImg: String,

    @Json(name = "bestAccuracy")
    val bestAccuracy:Float,

    @Json(name = "bestTime")
    val bestTime:Float,

    @Json(name = "winCount")
    val winCount:Int,

    @Json(name = "histories")
    val histories:List<YogaHistoryResponseDto>

)