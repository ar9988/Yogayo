package com.d104.data.remote.dto

data class PoseRecordRequestDto (
    val roomId:Long?,
    val accuracy:Float,
    val ranking:Int?,
    val poseTime:Float
)