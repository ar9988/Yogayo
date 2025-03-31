package com.d104.data.remote.dto

data class PoseRecordRequest (
    val roomRecordId:Long?,
    val accuracy:Float,
    val ranking:Int?,
    val poseTime:Float
)