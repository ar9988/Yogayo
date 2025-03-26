package com.d104.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class YogaPoseDto (

    @Json(name = "poseId")
    val poseId:Long,

    @Json(name = "poseName")
    val poseName:String,

    @Json(name = "poseDescription")
    val poseDescription: String,

    @Json(name = "poseImg")
    val poseImg:String,

    @Json(name = "poseLevel")
    val poseLevel:Int,

    @Json(name = "poseVideo")
    val poseVideo:String,

    @Json(name = "setPoseId")
    val setPoseId:Long?,

    @Json(name = "poseAnimation")
    val poseAnimation: String,

)