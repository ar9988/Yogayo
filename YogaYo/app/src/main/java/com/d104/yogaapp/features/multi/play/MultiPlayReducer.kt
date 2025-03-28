package com.d104.yogaapp.features.multi.play

import javax.inject.Inject

class MultiPlayReducer @Inject constructor() {
    fun reduce(currentState: MultiPlayState, intent: MultiPlayIntent): MultiPlayState {
        return when(intent){
            is MultiPlayIntent.UserLeft -> currentState.copy()
            is MultiPlayIntent.UpdateCameraPermission -> currentState.copy(
                cameraPermissionGranted = intent.granted
            )
            is MultiPlayIntent.UserJoined -> currentState.copy()
            is MultiPlayIntent.ClickMenu -> currentState.copy(
                menuClicked = !currentState.menuClicked
            )
            is MultiPlayIntent.ExitRoom -> currentState.copy(
                menuClicked = !currentState.menuClicked
            )
            is MultiPlayIntent.BackPressed -> {
                val previousState = when {
                    currentState.gameState.ordinal > 0 -> {
                        GameState.entries[currentState.gameState.ordinal - 1]
                    }
                    else -> currentState.gameState
                }
                currentState.copy(gameState = previousState)
            }
            is MultiPlayIntent.ClickPose -> {
                val nextState = when {
                    currentState.gameState.ordinal < GameState.entries.size - 1 -> {
                        GameState.entries[currentState.gameState.ordinal + 1]
                    }
                    else -> currentState.gameState
                }
                currentState.copy(
                    gameState = nextState,
                    selectedPoseId = intent.poseId
                )
            }
            is MultiPlayIntent.ClickNext -> {
                val nextState = when {
                    currentState.gameState.ordinal < GameState.entries.size - 1 -> {
                        GameState.entries[currentState.gameState.ordinal + 1]
                    }
                    else -> currentState.gameState
                }
                currentState.copy(gameState = nextState)
            }
            else -> currentState
        }
    }
}