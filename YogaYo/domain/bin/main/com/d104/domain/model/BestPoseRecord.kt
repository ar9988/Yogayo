package com.d104.domain.model

data class BestPoseRecord (
    val poseId: Long,
    val poseName: String,
    val poseImg: String,
    val bestAccuracy:Float,
    val bestTime:Float
)