package com.d104.yogaapp.features.multi.play

import com.d104.domain.model.PeerUser
import com.d104.domain.model.UserJoinedMessage
import com.d104.domain.model.UserLeftMessage
import com.d104.domain.model.UserReadyMessage
import javax.inject.Inject

class MultiPlayReducer @Inject constructor() {
    fun reduce(currentState: MultiPlayState, intent: MultiPlayIntent): MultiPlayState {
        return when (intent) {
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

            is MultiPlayIntent.InitializeRoom -> currentState.copy(
                currentRoom = intent.room
            )

            is MultiPlayIntent.ReceiveWebSocketMessage -> {
                when (intent.message.type) {
                    "user_joined" -> {
                        val userJoinedMessage = intent.message as UserJoinedMessage
                        val newPeerId = userJoinedMessage.peerId
                        currentState.copy(
                            userList = currentState.userList.apply {
                                this[newPeerId] = PeerUser(
                                    newPeerId,
                                    userJoinedMessage.userNickName
                                )
                            }
                        )
                    }

                    "user_left" -> {
                        val userLeftMessage = intent.message as UserLeftMessage

                        currentState.copy(
                            userList = currentState.userList.apply {
                                this.remove(userLeftMessage.peerId)
                            }
                        )
                    }

                    "user_ready" -> {
                        val userReadyMessage = intent.message as UserReadyMessage
                        currentState.copy(
                            userList = currentState.userList.apply {
                                this[userReadyMessage.peerId] = this[userReadyMessage.peerId]!!.copy(
                                    isReady = userReadyMessage.isReady
                                )
                            }
                        )
                    }
                    "game_started" -> currentState.copy(
                        gameState = GameState.Playing
                    )
                    "game_end" -> currentState.copy(
                        gameState = GameState.GameResult
                    )
                    else -> currentState
                }
            }

            is MultiPlayIntent.ReceiveWebRTCImage -> {
                currentState.copy(
                    peerImage = intent.bitmap
                )
            }

            else -> currentState
        }
    }
}