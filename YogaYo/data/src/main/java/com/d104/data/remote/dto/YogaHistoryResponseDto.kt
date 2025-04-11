package com.d104.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class YogaHistoryResponseDto(
    @Json(name = "historyId")
    val historyId:Long,

    @Json(name = "userId")
    val userId:Long,

    @Json(name="accuracy")
    val accuracy:Float,

    @Json(name="ranking")
    val ranking:Int?,

    @Json(name="poseTime")
    val poseTime:Float,

    @Json(name="recordImg")
    val recordImg:String?,

    @Json(name="createdAt")
    val createdAt:Long

)
