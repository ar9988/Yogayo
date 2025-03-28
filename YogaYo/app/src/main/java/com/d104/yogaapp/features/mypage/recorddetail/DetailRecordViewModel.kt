package com.d104.yogaapp.features.mypage.recorddetail

import androidx.lifecycle.ViewModel
import com.d104.domain.model.BestPoseRecord
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class DetailRecordViewModel @Inject constructor(
    private val reducer: DetailRecordReducer
): ViewModel() {
    private val _state = MutableStateFlow(DetailRecordState())
    val state : StateFlow<DetailRecordState> = _state.asStateFlow()
    val tmpPoseHistories: List<BestPoseRecord> = listOf(
        BestPoseRecord(
            poseId = 1,
            poseName = "나무자세",
            poseImg = "https://yogayo.s3.ap-northeast-2.amazonaws.com/image+(2).png",
            bestAccuracy = 92.34f,
            bestTime = 11.20f,
        ),
        BestPoseRecord(
            poseId = 1,
            poseName = "나무자세",
            poseImg = "https://yogayo.s3.ap-northeast-2.amazonaws.com/image+(2).png",
            bestAccuracy = 92.34f,
            bestTime = 11.20f,
        ),
        BestPoseRecord(
            poseId = 1,
            poseName = "나무자세",
            poseImg = "https://yogayo.s3.ap-northeast-2.amazonaws.com/image+(2).png",
            bestAccuracy = 92.34f,
            bestTime = 11.20f,
        ),
        BestPoseRecord(
            poseId = 1,
            poseName = "나무자세",
            poseImg = "https://yogayo.s3.ap-northeast-2.amazonaws.com/image+(2).png",
            bestAccuracy = 92.34f,
            bestTime = 11.20f,
        ),
        BestPoseRecord(
            poseId = 1,
            poseName = "나무자세",
            poseImg = "https://yogayo.s3.ap-northeast-2.amazonaws.com/image+(2).png",
            bestAccuracy = 92.34f,
            bestTime = 11.20f,
        ),
        BestPoseRecord(
            poseId = 1,
            poseName = "나무자세",
            poseImg = "https://yogayo.s3.ap-northeast-2.amazonaws.com/image+(2).png",
            bestAccuracy = 92.34f,
            bestTime = 11.20f,
        ),
    )

    init{
        handleIntent(DetailRecordIntent.SetBestPoseHistories(tmpPoseHistories))
    }

    fun handleIntent(intent: DetailRecordIntent) {
        val newState = reducer.reduce(state.value, intent)

        // 상태 업데이트
        _state.value = newState
        when (intent) {
            is DetailRecordIntent.SetUserRecord -> {}
            is DetailRecordIntent.SetBestPoseHistories -> {}

        }
    }


}