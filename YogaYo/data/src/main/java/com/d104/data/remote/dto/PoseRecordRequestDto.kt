package com.d104.data.remote.dto

data class PoseRecordRequestDto (
    val roomRecordId:Long?,
    val accuracy:Float,
    val ranking:Int?,
    val poseTime:Float
)