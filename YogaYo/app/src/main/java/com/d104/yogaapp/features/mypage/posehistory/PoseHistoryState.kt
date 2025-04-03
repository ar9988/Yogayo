package com.d104.yogaapp.features.mypage.posehistory

import com.d104.domain.model.BestPoseRecord
import com.d104.domain.model.YogaPoseHistoryDetail
import com.d104.domain.model.YogaPoseRecord
import com.d104.yogaapp.features.solo.play.DownloadState

data class PoseHistoryState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val poseDetail: YogaPoseHistoryDetail = YogaPoseHistoryDetail(
        poseId = -1,
        poseName = "",
        poseImg = "",
        bestAccuracy = 0f,
        bestTime = 0f,
        winCount = 0,
        histories = emptyList()
    ),
    val downloadState: DownloadState = DownloadState.Default,
    // 선택된 포즈의 기본 정보 (이름, 이미지 등)
//    val historyList: List<YogaPoseRecord> = emptyList(),
//    val chartData: List<Pair<String, Float>> = emptyList(), // 차트 표시용 데이터
//    val poseExecutionCount: Int = 0, // 자세 수행 횟수
//    val bestAccuracy: Float = 0f, // 베스트 정확도 (BestPoseRecord에서 가져옴)
//    val bestTime: Float = 0f // 최대 유지 시간 (BestPoseRecord에서 가져옴) - YogaPoseRecord에 없으므로 필요 시 BestPoseRecord 사용
)