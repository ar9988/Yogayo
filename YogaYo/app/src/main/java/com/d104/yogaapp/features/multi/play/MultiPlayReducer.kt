package com.d104.yogaapp.features.multi.play

import com.d104.domain.model.PeerUser
import com.d104.domain.model.UserJoinedMessage
import com.d104.domain.model.UserLeftMessage
import com.d104.domain.model.UserReadyMessage
import timber.log.Timber
import javax.inject.Inject

class MultiPlayReducer @Inject constructor() {
    fun reduce(currentState: MultiPlayState, intent: MultiPlayIntent): MultiPlayState {
        return when (intent) {
            is MultiPlayIntent.UserLeft -> {
                val newUserList = currentState.userList.toMutableMap().apply {
                    remove(intent.userId)
                }
                Timber.d("user_left ${intent.userId}")
                currentState.copy(userList = newUserList)
            }

            is MultiPlayIntent.UpdateCameraPermission -> currentState.copy(
                cameraPermissionGranted = intent.granted
            )

            is MultiPlayIntent.UserJoined -> {
                val user = intent.user
                val newUserList = currentState.userList.toMutableMap().apply {
                    put(
                        user.id, PeerUser(
                            user.id,
                            user.nickName
                        )
                    )
                }
                Timber.d("user_joined ${user.id}")
                currentState.copy(userList = newUserList)

            }

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

                    "user_ready" -> {
                        val userReadyMessage = intent.message as UserReadyMessage
                        val newUserList = currentState.userList.toMutableMap().apply {
                            this[userReadyMessage.fromPeerId]?.let { user ->
                                put(
                                    userReadyMessage.fromPeerId,
                                    user.copy(isReady = userReadyMessage.isReady)
                                )
                            }
                        }
                        currentState.copy(userList = newUserList)
                    }
                    "round_end" -> currentState.copy(
                        gameState = GameState.RoundResult
                    )

                    "game_end" -> currentState.copy(
                        gameState = GameState.GameResult
                    )

                    else -> currentState
                }
            }


            is MultiPlayIntent.GameStarted -> {
                //yoga 리스트에서 0번 인덱스로 설정하기
                currentState.copy(
                    gameState = GameState.Playing,
                    roundIndex = 0,
                    currentPose = currentState.currentRoom!!.userCourse.poses[0],
                )
            }

            is MultiPlayIntent.RoundStarted -> {
                currentState.copy(
                    gameState = GameState.Playing,
                    roundIndex = intent.state,
                    currentPose = currentState.currentRoom!!.userCourse.poses[intent.state],
                )
            }

            is MultiPlayIntent.ReceiveWebRTCImage -> {
                currentState.copy(
                    bitmap = intent.bitmap
                )
            }

            is MultiPlayIntent.UpdateTimerProgress -> {
                currentState.copy(
                    timerProgress = intent.progress
                )
            }

            is MultiPlayIntent.UpdateScore -> {
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