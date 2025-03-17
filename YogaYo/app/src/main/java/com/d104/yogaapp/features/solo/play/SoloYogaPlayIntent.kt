package com.d104.yogaapp.features.solo.play

sealed class SoloYogaPlayIntent {
    object TogglePlayPause : SoloYogaPlayIntent()
    object SkipPose : SoloYogaPlayIntent()
    object RestartCurrentPose : SoloYogaPlayIntent() // 현재 동작 다시 시작
    object Exit : SoloYogaPlayIntent()
    data class UpdateTimerProgress(val progress: Float) : SoloYogaPlayIntent()
    data class UpdateCameraPermission(val granted: Boolean) : SoloYogaPlayIntent()
}