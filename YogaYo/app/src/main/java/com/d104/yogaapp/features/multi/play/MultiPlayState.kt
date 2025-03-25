package com.d104.yogaapp.features.multi.play

import com.d104.domain.model.User
import com.d104.domain.model.YogaPose

data class MultiPlayState(
    val userList: MutableMap<Int, User> = mutableMapOf(),
    val cameraPermissionGranted: Boolean = false,
    val menuClicked: Boolean = false,
    val isPlaying: Boolean = false,
    val timerProgress: Float = 1.0f, // 1.0 = 100% (20초), 0.0 = 0% (0초)
    val isCountingDown: Boolean = false,
    val currentPose: YogaPose = null
)