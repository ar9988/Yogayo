package com.d104.yogaapp.features.multi.play

import android.graphics.Bitmap

sealed class MultiPlayIntent {
    data class UserJoined(val userId: String) : MultiPlayIntent()
    data class UserLeft(val userId: String) : MultiPlayIntent()
    data class UserReady(val userId: String) : MultiPlayIntent()
    data class UserNotReady(val userId: String) : MultiPlayIntent()
    data class GameStarted(val userId: String) : MultiPlayIntent()
    data class UpdateCameraPermission(val granted: Boolean) : MultiPlayIntent()
    data class CaptureImage(val bitmap: Bitmap) : MultiPlayIntent()
    data class ClickPose(val poseId: Int) : MultiPlayIntent()
    data object ExitRoom: MultiPlayIntent()
    data object ClickMenu : MultiPlayIntent()
    data object BackPressed: MultiPlayIntent()
    data object ClickNext : MultiPlayIntent()
}