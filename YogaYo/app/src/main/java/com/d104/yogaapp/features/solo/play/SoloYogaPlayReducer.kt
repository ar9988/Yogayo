package com.d104.yogaapp.features.solo.play

import com.d104.domain.model.YogaHistory
import timber.log.Timber
import javax.inject.Inject

class SoloYogaPlayReducer @Inject constructor() {

    fun reduce(state: SoloYogaPlayState, intent: SoloYogaPlayIntent): SoloYogaPlayState {
        return when (intent) {
            is SoloYogaPlayIntent.TogglePlayPause -> {
                val newIsPlaying = !state.isPlaying
                state.copy(isPlaying = newIsPlaying)
            }
            is SoloYogaPlayIntent.GoToNextPose -> {
                if(state.currentPoseIndex<state.userCourse.poses.size-1) {
                    state.copy(
                        currentPoseIndex = state.currentPoseIndex + 1,
                        timerProgress = 1f,
                        isPlaying = false,
                        isGuide = true,
                        remainingTime = 0f,
                        currentAccuracy = 0f,
                    )
                }else{
                    state.copy(isPlaying = false, isResult = true)
                }
            }
            is SoloYogaPlayIntent.RestartCurrentPose -> {
                // 현재 포즈 유지하고 타이머만 리셋
                state.copy(
                    timerProgress = 1f,
                    isPlaying = true,
                    isCountingDown = true
                )
            }
            is SoloYogaPlayIntent.UpdateTimerProgress -> {
                state.copy(timerProgress = intent.progress)
            }
            is SoloYogaPlayIntent.UpdateCameraPermission -> {
                state.copy(cameraPermissionGranted = intent.granted)
            }
            is SoloYogaPlayIntent.InitializeWithCourse->{
                state.copy(userCourse = intent.course)
            }
            SoloYogaPlayIntent.ExitGuide -> {
                state.copy(
                    timerProgress = 1f,
                    isPlaying = true,
                    isGuide = false
                )
            }
            SoloYogaPlayIntent.Exit -> {state}
            SoloYogaPlayIntent.FinishCountdown -> {
                state.copy (
                    isCountingDown = false
                )
            }
            SoloYogaPlayIntent.StartCountdown -> {
                state.copy (
                    isCountingDown = true,
                    isPlaying = true
                )
            }

            SoloYogaPlayIntent.SkipPose -> {
                // 현재 포즈에 대한 히스토리 생성 (isSkipped = true로 표시)
                val currentIndex = state.currentPoseIndex
                val currentPose = if (state.userCourse.poses.isNotEmpty() &&
                    currentIndex < state.userCourse.poses.size) {
                    state.userCourse.poses[currentIndex]
                } else null

                val updatedHistories = state.poseHistories.toMutableList()

                // 현재 인덱스 위치에 스킵된 히스토리 추가
                if (currentIndex < updatedHistories.size) {
                    // 이미 히스토리가 있으면 스킵 표시로 업데이트
                    updatedHistories[currentIndex] = updatedHistories[currentIndex].copy(isSkipped = true)
                } else {
                    // 없으면 새로 생성
                    while (updatedHistories.size < currentIndex) {
                        updatedHistories.add(
                            YogaHistory(
                                poseId = 0,
                                poseName = "",
                                accuracy = 0f,
                                recordImg = "",
                                isSkipped = true,
                                poseImg = ""
                            )
                        )
                    }
                    // 현재 인덱스에 스킵된 히스토리 추가
                    updatedHistories.add(
                        YogaHistory(
                            poseId = currentPose?.poseId ?: 0,
                            poseName = currentPose?.poseName ?: "알 수 없는 포즈",
                            accuracy = 0f,
                            recordImg = "",
                            isSkipped = true,
                            poseImg = ""
                        )
                    )
                }

                // 다음 포즈로 이동하고 히스토리 업데이트
                if(state.currentPoseIndex < state.userCourse.poses.size - 1) {
                    state.copy(
                        currentPoseIndex = state.currentPoseIndex + 1,
                        timerProgress = 1f,
                        isPlaying = false,
                        isGuide = true,
                        isSkipped = true,
                        poseHistories = updatedHistories,
                        remainingTime = 0f,
                        currentAccuracy = 0f,
                    )
                } else {
                    state.copy(
                        isPlaying = false,
                        isResult = true,
                        isSkipped = true,
                        remainingTime = 0f,
                        poseHistories = updatedHistories
                    )
                }
            }

            is SoloYogaPlayIntent.DownloadImage -> {
                state
            }
            SoloYogaPlayIntent.ResetDownloadState -> {
                state.copy(
                    downloadState = DownloadState.Default
                )

            }
            is SoloYogaPlayIntent.SetLoginState -> {
                state.copy(
                    isLogin = intent.isLogin
                )
            }

            is SoloYogaPlayIntent.SendHistory -> state
            is SoloYogaPlayIntent.SetCurrentHistory -> {
                Timber.d("remainingTime:${intent.time}")
                state.copy(currentAccuracy = intent.accuracy, remainingTime = intent.time)
            }
        }
    }

}