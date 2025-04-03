package com.d104.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class PoseRecordResponseDto(
    @Json(name = "poseRecordId")
    val poseRecordId:Long,

    @Json(name = "poseId")
    val poseId:Long,

    @Json(name = "roomRecordId")
    val roomRecordId:Long?,

    @Json(name = "accuracy")
    val accuracy:Float,

    @Json(name = "ranking")
    val ranking:Int?,

    @Json(name = "poseTime")
    val poseTime:Float,

    @Json(name= "recordImg")
    val recordImg:String,

    @Json(name = "createdAt")
    val createdAt:Long
)
