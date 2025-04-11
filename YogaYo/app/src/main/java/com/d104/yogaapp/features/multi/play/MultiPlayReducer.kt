package com.d104.yogaapp.features.multi.play

import android.graphics.Bitmap
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

            is MultiPlayIntent.SetBestImage -> {
                Timber.d("Reducer: Handling SetBestImage with ${intent.bitmap}")
                val newBitmap = Bitmap.createBitmap(intent.bitmap)
                currentState.copy(
                    bestBitmap = newBitmap,
                    isLoading = false
                )
            }

            is MultiPlayIntent.SendHistory -> {
                Timber.d("Reducer: Handling SendHistory with ${intent.bitmap}")
                currentState.copy(
                    accuracy = intent.accuracy,
                    time = intent.time,
//                    time = currentState.myId!!.toFloat(), //test용 임의 값
                    bitmap = intent.bitmap,
                    beyondPose = intent.pose,
                )

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
                            user.nickName,
                            iconUrl = user.iconUrl,
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
                menuClicked = false
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

            is MultiPlayIntent.SetErrorMessage -> {
                Timber.d("Reducer: Handling SetErrorMessage with ${intent.e}")
                currentState.copy(
                    errorMsg = intent.e,
                    gameState = GameState.GameResult
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

            is MultiPlayIntent.Exit -> currentState.copy(
                exit = true
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
                    "user_not_ready" -> {
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
                    "game_end" -> currentState.copy(
                        gameState = GameState.GameResult
                    )

                    else -> currentState
                }
            }

            is MultiPlayIntent.SetCurrentHistory -> {
                Timber.d("Reducer: Handling SetCurrentHistory with accuracy ${intent.accuracy} and time ${intent.time}")
                currentState.copy(
                    currentAccuracy = intent.accuracy,
                )
            }

            is MultiPlayIntent.GameStarted -> {
                //yoga 리스트에서 0번 인덱스로 설정하기
                Timber.d("game_started")
                currentState.copy(
                    gameState = GameState.Playing,
                    roundIndex = 0,
                    currentPose = currentState.currentRoom!!.userCourse.poses[0],
                )
            }

            is MultiPlayIntent.RoundEnded -> {
                Timber.d("Round: Handling RoundEnded")
                currentState.copy(
                    gameState = GameState.RoundResult,
                    isLoading = true, // 로딩 시작!
                    bestBitmap = null      // 이전 이미지 초기화
                )
            }

            is MultiPlayIntent.RoundStarted -> {
                Timber.d("Round: Handling RoundStarted for state ${intent.state}")

                // 1. 모든 사용자의 roundScore를 0으로 초기화한 새로운 userList 생성
                val updatedUserList = currentState.userList.mapValues { entry ->
                    // 각 사용자의 PeerUser 객체를 복사하되, roundScore만 0.0f로 설정
                    entry.value.copy(roundScore = 0.0f)
                }
                Timber.d("Reducer: User round scores reset.")

                // 2. 다음 라운드 포즈 안전하게 가져오기 (NullPointerException 방지)
                val nextPose = currentState.currentRoom?.userCourse?.poses?.getOrNull(intent.state)

                // 3. 상태 업데이트
                if (nextPose != null) {
                    Timber.d("Reducer: Setting gameState to Playing, updating roundIndex, currentPose, and userList.")
                    currentState.copy(
                        gameState = GameState.Playing, // 게임 상태를 Playing으로
                        roundIndex = intent.state,      // 현재 라운드 인덱스 업데이트
                        currentPose = nextPose,         // 현재 포즈 업데이트 (안전하게 가져온 값 사용)
                        userList = updatedUserList,
                        timerProgress = 1.0f // 점수가 초기화된 사용자 목록으로 교체
                    )
                } else {
                    // 다음 포즈 정보를 가져올 수 없는 경우 (오류 상황)
                    Timber.e("Reducer: Cannot start round ${intent.state}, next pose data is missing!")
                    // 오류 처리: 일단 점수는 초기화하고, 상태는 Playing으로 가되 포즈는 이전 것을 유지하거나 기본값 사용?
                    // 또는 특정 Error 상태로 전환할 수도 있습니다.
                    // 여기서는 점수만 초기화하고 나머지는 최대한 진행하는 것으로 가정합니다.
                    currentState.copy(
                        gameState = GameState.Playing,      // 게임 상태는 Playing으로 시도
                        roundIndex = intent.state,          // 라운드 인덱스 업데이트
                        // currentPose는 이전 상태 유지 또는 기본값 설정 필요
                        userList = updatedUserList          // 점수가 초기화된 사용자 목록으로 교체
                        // currentPose = 기본포즈 or currentState.currentPose // 필요에 따라 주석 해제/수정
                    )
                }
            }

            is MultiPlayIntent.ReceiveWebRTCImage -> {
                Timber.d("Reducer: Received complete WebRTC image.")
                val newBitmap = Bitmap.createBitmap(intent.bitmap)
                currentState.copy(
                    bestBitmap = newBitmap, // 수신된 비트맵으로 업데이트
                    isLoading = false,      // 로딩 종료!
                )
            }

            is MultiPlayIntent.GameEnd -> {
                Timber.d("Reducer: Handling GameEnd.")
                currentState.copy(
                    gameState = GameState.GameResult
                )
            }

            is MultiPlayIntent.UpdateTimerProgress -> {
                currentState.copy(
                    timerProgress = intent.progress
                )
            }

            is MultiPlayIntent.BestPose -> {
                Timber.d("Reducer: Handling BestPose with ${intent.it}")
                currentState.copy(
                    bestUrls = intent.it
                )
            }

            is MultiPlayIntent.AllPose -> {
                Timber.d("Reducer: Handling AllPose with ${intent.it}")
                currentState.copy(
                    allUrls = intent.it
                )
            }

            is MultiPlayIntent.UpdateTotalScore ->{
                Timber.d("Reducer: Handling UpdateTotalScore with ${intent.score}")
                val score = intent.score
                val userId = intent.peerId
                // 기존 사용자 데이터 가져오기
                val user = currentState.userList[userId]

                if (user != null) {
                    // 사용자 데이터 업데이트
                    val updatedUser = user.copy(
                        totalScore = user.totalScore+score
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

            is MultiPlayIntent.UpdateScore -> {
                Timber.d("Reducer: Handling UpdateScore with ${intent.scoreUpdateMessage}")
                val score = intent.scoreUpdateMessage.time
                val userId = intent.id
                // 기존 사용자 데이터 가져오기
                val user = currentState.userList[userId]

                if (user != null) {
                    // 사용자 데이터 업데이트
                    val updatedUser = user.copy(
                        roundScore = score,
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