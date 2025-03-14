package com.d104.yogaapp.features.solo

sealed class SoloYogaPlayIntent {
    object TogglePlayPause : SoloYogaPlayIntent()
    object SkipPose : SoloYogaPlayIntent()
    data class UpdateTimerProgress(val progress: Float) : SoloYogaPlayIntent()
}