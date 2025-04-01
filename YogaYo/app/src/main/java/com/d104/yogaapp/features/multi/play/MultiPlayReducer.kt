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
                    "round_start" -> currentState.copy(
                        roundIndex = currentState.roundIndex+1,
                        gameState = GameState.Playing
                        //기타 스테이트 처리
                    )
                    "round_end" -> currentState.copy(
                        gameState = GameState.RoundResult
                    )
                    "game_end" -> currentState.copy(
                        gameState = GameState.GameResult
                    )
                    else -> currentState
                }
            }

            is MultiPlayIntent.ReceiveWebRTCImage -> {
                currentState.copy(
                    bitmap = intent.bitmap
                )
            }

            is MultiPlayIntent.UpdateScore ->{
                val score = intent.scoreUpdateMessage.score
                val userId = intent.id
                // 기존 사용자 데이터 가져오기
                val user = currentState.userList[userId]

                if (user != null) {
                    // 사용자 데이터 업데이트
                    val updatedUser = user.copy(
                        roundScore = score
                    )

                    // 상태 복사 및 업데이트
                    currentState.copy(
                        userList = currentState.userList.toMutableMap().apply {
                            this[userId] = updatedUser
                        }
                    )
                } else {
                    currentState // 사용자가 없는 경우 상태를 변경하지 않음
                }
            }

            else -> currentState
        }
    }
}