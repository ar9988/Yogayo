package com.d104.yogaapp.features.solo.play

import com.d104.domain.model.UserCourse
import com.d104.domain.model.YogaHistory

data class SoloYogaPlayState(
    val userCourse: UserCourse = UserCourse(0,"",false, emptyList()),
    val currentPoseIndex: Int = 0,
    val isPlaying: Boolean = false,
    val timerProgress: Float = 1.0f, // 1.0 = 100% (20초), 0.0 = 0% (0초)
    val cameraPermissionGranted: Boolean = false,
    val isResult: Boolean = false,
    val isSkipped: Boolean = false,
    val isGuide: Boolean = true,
    val currentAccuracy: Float = 0f,
    val remainingTime: Float = 0f,
    val isCountingDown: Boolean = false,
    val poseHistories:MutableList<YogaHistory> = mutableListOf(),
    val downloadState: DownloadState = DownloadState.Default,
    val isLogin:Boolean = false

)
sealed class DownloadState {
    object Default : DownloadState()
    object Loading : DownloadState()
    object Success : DownloadState()
    data class Error(val message: String) : DownloadState()
}
