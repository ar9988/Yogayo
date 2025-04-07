package com.d104.yogaapp.features.mypage.posehistory

import com.d104.domain.model.YogaPoseHistoryDetail

sealed class PoseHistoryIntent {
    data class Initialize(val poseId: Long) : PoseHistoryIntent()
    // 데이터 로딩 성공 시 전달할 Intent (예시, 실제 필요한 데이터로 구성)
    data class SetPoseHistoryData(
        val yogaPoseHistoryDetail: YogaPoseHistoryDetail
    ) : PoseHistoryIntent()
    data class ShowError(val message: String) : PoseHistoryIntent()
    object SetLoading : PoseHistoryIntent()
    object ResetDownloadState: PoseHistoryIntent()
}