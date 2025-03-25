package com.d104.yogaapp.features.solo.play

import android.graphics.Bitmap
import android.media.Image
import android.net.Uri
import com.d104.domain.model.UserCourse
import com.d104.yogaapp.features.solo.SoloIntent

sealed class SoloYogaPlayIntent {
    object TogglePlayPause : SoloYogaPlayIntent()
    object GoToNextPose : SoloYogaPlayIntent()
    object SkipPose : SoloYogaPlayIntent()
    object RestartCurrentPose : SoloYogaPlayIntent() // 현재 동작 다시 시작
    object Exit : SoloYogaPlayIntent()
    object ExitGuide: SoloYogaPlayIntent()
    object StartCountdown : SoloYogaPlayIntent()
    object FinishCountdown : SoloYogaPlayIntent()
    object ResetDownloadState: SoloYogaPlayIntent()
    data class UpdateTimerProgress(val progress: Float) : SoloYogaPlayIntent()
    data class UpdateCameraPermission(val granted: Boolean) : SoloYogaPlayIntent()
    data class InitializeWithCourse(val course: UserCourse) : SoloYogaPlayIntent()
    data class CaptureImage(val bitmap: Bitmap): SoloYogaPlayIntent()
    data class DownloadImage(val uri:Uri,val poseName:String):SoloYogaPlayIntent()

}