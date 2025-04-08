package com.d104.yogaapp.features.multi.play

import android.graphics.Bitmap
import com.d104.domain.model.PeerUser
import com.d104.domain.model.Room
import com.d104.domain.model.ScoreUpdateMessage
import com.d104.domain.model.SignalingMessage
import com.d104.domain.model.YogaPose

sealed class MultiPlayIntent {
    data class UserJoined(val user: PeerUser) : MultiPlayIntent()
    data class UserLeft(val userId: String) : MultiPlayIntent()
    data class UserReady(val userId: String) : MultiPlayIntent()
    data class UserNotReady(val userId: String) : MultiPlayIntent()
    data object GameStarted : MultiPlayIntent()
    data class UpdateCameraPermission(val granted: Boolean) : MultiPlayIntent()
    data class CaptureImage(val bitmap: Bitmap) : MultiPlayIntent()
    data class ClickPose(val poseId: Int) : MultiPlayIntent()
    data class InitializeRoom(val room: Room) : MultiPlayIntent()
    data class ReceiveWebSocketMessage(val message: SignalingMessage) : MultiPlayIntent()
    data class ReceiveWebRTCImage(val bitmap:Bitmap) : MultiPlayIntent()
    data class UpdateScore(val id: String,val scoreUpdateMessage: ScoreUpdateMessage) : MultiPlayIntent()
    data class RoundStarted(val state: Int) : MultiPlayIntent()
    data class UpdateTimerProgress(val progress: Float) : MultiPlayIntent()
    data class SendHistory(val pose: YogaPose,val accuracy: Float,val time: Float,val bitmap: Bitmap) : MultiPlayIntent()
    data class SetCurrentHistory(val accuracy: Float,val time: Float) : MultiPlayIntent()
    data object ExitRoom: MultiPlayIntent()
    data object ClickMenu : MultiPlayIntent()
    data object BackPressed: MultiPlayIntent()
    data object ClickNext : MultiPlayIntent()
    data object RoundEnded: MultiPlayIntent()
    data object Exit: MultiPlayIntent()
    data object ReadyClick: MultiPlayIntent()
    data object GameEnd: MultiPlayIntent()
}