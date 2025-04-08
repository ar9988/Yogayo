package com.d104.yogaapp.features.multi.play

import android.graphics.Bitmap
import com.d104.domain.model.PeerUser
import com.d104.domain.model.Room
import com.d104.domain.model.YogaPose

data class MultiPlayState(
    val userList: Map<String, PeerUser> = emptyMap(),
    val cameraPermissionGranted: Boolean = false,
    val menuClicked: Boolean = false,
    val isPlaying: Boolean = true,
    val timerProgress: Float = 1.0f, // 1.0 = 100% (20초), 0.0 = 0% (0초)
    val isCountingDown: Boolean = false,
    val currentPose: YogaPose = YogaPose(0, "", "", 0, listOf("나무 자세 설명"), "", 0,""),
    val currentAccuracy: Float = 0.0f,
    val gameState: GameState = GameState.Waiting,
    val second : Float = 0.0f, // 1.0 = 3초, 0.0 = 0초,
    val score : Float = 0.0f,
    val selectedPoseId :Int = 0,
    val currentRoom: Room? = null,
    val bitmap: Bitmap? = null,
    val roundIndex: Int = 0,
    val exit: Boolean = false,
    val myId: String? = null,
    val isLoading: Boolean = true,
    val bestUrls: List<String> = emptyList(),
    val urls: Map<Int,List<String>> = emptyMap(),
    val uri: String = "",
    val beyondPose: YogaPose = YogaPose(0, "", "", 0, listOf("나무 자세 설명"), "", 0,""),
    val accuracy: Float = 0.0f,
    val time: Float = 0.0f,
)

enum class GameState {
    Waiting, Playing, RoundResult, GameResult, Gallery, Detail
}