package com.d104.domain.model

data class YogaPose (
    val poseId:Long,
    val poseName:String,
    val poseImg:String,
    val poseLevel:Int,
    val poseDescriptions: List<String>,
    val poseAnimation: String,
    val setPoseId:Long,
    val poseVideo:String,
)