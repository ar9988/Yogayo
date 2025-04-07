package com.d104.domain.model

data class YogaPoseRecord (
    val poseRecordId:Long = -1,
    val poseId:Long = -1,
    val roomRecordId:Long? = null,
    val accuracy:Float = 0.5f,
    val ranking:Int? = null,
    val poseTime:Float = 0.0f,
    val recordTime:String = "",
    val createdAt:Long? = null
)