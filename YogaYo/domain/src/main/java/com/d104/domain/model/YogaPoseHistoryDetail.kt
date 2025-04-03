package com.d104.domain.model

data class YogaPoseHistoryDetail (
    val poseId: Long,
    val poseName: String,
    val poseImg: String,
    val bestAccuracy:Float,
    val bestTime:Float,
    val winCount:Int,
    val histories: List<YogaHistory>
)