package com.d104.domain.model

data class YogaHistory(
    val poseId:Long,
    val poseName:String,
    val roomRecordId:Int = -1,
    val userId:Int = -1,
    val accuracy:Float,
    val poseTime:Float = 5.5f,
    val recordImg:String,
    val isSkipped: Boolean = false,
    val poseImg:String
)