package com.d104.yogaapp.features.solo

import com.d104.domain.model.YogaPose
import javax.inject.Inject

class SoloYogaPlayReducer @Inject constructor() {

    fun reduce(currentState: SoloYogaPlayState, intent: SoloYogaPlayIntent): SoloYogaPlayState {
        return when (intent) {
            is SoloYogaPlayIntent.TogglePlayPause ->
                currentState.copy(isPlaying = !currentState.isPlaying)

            is SoloYogaPlayIntent.SkipPose -> {
                // 다음 포즈 로직 구현 (예시)
                val nextPose = getNextPose(currentState.currentPose)
                currentState.copy(
                    currentPose = nextPose,
                    timerProgress = 1.0f
                )
            }

            is SoloYogaPlayIntent.UpdateTimerProgress ->
                currentState.copy(timerProgress = intent.progress)
        }
    }

    // 다음 포즈를 결정하는 헬퍼 메서드 (예시)
    private fun getNextPose(currentPose: YogaPose): YogaPose {
        // 실제 구현에서는 포즈 목록에서 다음 포즈를 가져오는 로직 구현
        return currentPose
    }
}