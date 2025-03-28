package com.d104.yogaapp.features.multi.play

import com.d104.domain.model.PeerUser
import com.d104.domain.model.YogaPose

data class MultiPlayState(
    val userList: MutableMap<Int, PeerUser> = mutableMapOf(
        0 to PeerUser("user1", true, 0),
        1 to PeerUser("user2", false, 0),
        2 to PeerUser("uesr3", false, 0),
        3 to PeerUser("asfa", false, 0),
    ),
    val cameraPermissionGranted: Boolean = false,
    val menuClicked: Boolean = false,
    val isPlaying: Boolean = true,
    val timerProgress: Float = 1.0f, // 1.0 = 100% (20초), 0.0 = 0% (0초)
    val isCountingDown: Boolean = false,
    val currentPose: YogaPose = YogaPose(0, "", "", 0, listOf("나무 자세 설명"), "", 0,""),
    val currentAccuracy: Float = 0.0f,
    val gameState: GameState = GameState.GameResult,
    val second : Float = 1.0f, // 1.0 = 3초, 0.0 = 0초
    val selectedPoseId :Int = 0
)

enum class GameState {
    Waiting, Playing, RoundResult, GameResult, Gallery, Detail
}