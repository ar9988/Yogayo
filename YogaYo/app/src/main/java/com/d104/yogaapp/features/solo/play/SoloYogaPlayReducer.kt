package com.d104.yogaapp.features.solo.play

import com.d104.domain.model.YogaPose
import javax.inject.Inject

class SoloYogaPlayReducer @Inject constructor() {

    fun reduce(state: SoloYogaPlayState, intent: SoloYogaPlayIntent): SoloYogaPlayState {
        return when (intent) {
            is SoloYogaPlayIntent.TogglePlayPause -> {
                val newIsPlaying = !state.isPlaying
                state.copy(isPlaying = newIsPlaying)
            }
            is SoloYogaPlayIntent.SkipPose -> {
                val nextPose = getNextPose(state.currentPose)
                state.copy(
                    currentPose = nextPose,
                    timerProgress = 1f,
                    isPlaying = true
                )
            }
            is SoloYogaPlayIntent.RestartCurrentPose -> {
                // 현재 포즈 유지하고 타이머만 리셋
                state.copy(
                    timerProgress = 1f,
                    isPlaying = true
                )
            }
            is SoloYogaPlayIntent.UpdateTimerProgress -> {
                state.copy(timerProgress = intent.progress)
            }
            is SoloYogaPlayIntent.UpdateCameraPermission -> {
                state.copy(cameraPermissionGranted = intent.granted)
            }
            is SoloYogaPlayIntent.Exit -> {
                // Exit는 실제로 화면에서 나가는 동작이므로 상태 변경은 필요 없음
                state
            }
        }
    }

    // 다음 포즈를 결정하는 헬퍼 메서드 (예시)
    private fun getNextPose(currentPose: YogaPose): YogaPose {
        // 실제 구현에서는 포즈 목록에서 다음 포즈를 가져오는 로직 구현
        return currentPose
    }
}