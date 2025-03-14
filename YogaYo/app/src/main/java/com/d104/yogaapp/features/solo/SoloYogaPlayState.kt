package com.d104.yogaapp.features.solo

import com.d104.domain.model.YogaPose

data class SoloYogaPlayState(
    val currentPose: YogaPose = YogaPose(1,"자세 이름","",1,"설명","",2),
    val isPlaying: Boolean = true,
    val timerProgress: Float = 1.0f // 1.0 = 100% (20초), 0.0 = 0% (0초)
)
