package com.d104.domain.model

data class YogaPose (
    val poseId:Long,
    val poseName:String,
    val poseImg:String,
    val poseLevel:Int,
    val poseDescription: String,
    val poseVideo: String,
    val setPoseId:Long
)