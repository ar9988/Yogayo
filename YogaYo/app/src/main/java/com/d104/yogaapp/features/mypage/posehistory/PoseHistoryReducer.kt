package com.d104.yogaapp.features.mypage.posehistory

import com.d104.yogaapp.features.solo.play.DownloadState
import javax.inject.Inject

class PoseHistoryReducer @Inject constructor() {
    fun reduce(currentState: PoseHistoryState, intent: PoseHistoryIntent): PoseHistoryState {
        return when (intent) {
            is PoseHistoryIntent.Initialize -> {
                currentState
//                currentState.copy(isLoading = true, error = null) // 초기화 시 로딩 시작
            }
            is PoseHistoryIntent.SetLoading -> {
                currentState.copy(isLoading = true, error = null)
            }
            is PoseHistoryIntent.SetPoseHistoryData -> {
                currentState.copy(
                    isLoading = false,
                    error = null,
                    poseDetail = intent.yogaPoseHistoryDetail,
                )
            }
            is PoseHistoryIntent.ShowError -> {
                currentState.copy(isLoading = false, error = intent.message)
            }

            PoseHistoryIntent.ResetDownloadState -> {
                currentState.copy(
                    downloadState = DownloadState.Default
                )

            }
        }
    }
}