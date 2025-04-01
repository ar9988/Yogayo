package com.d104.yogaapp.features.mypage.recorddetail

import javax.inject.Inject

class DetailRecordReducer @Inject constructor() {

    fun reduce(state:DetailRecordState, intent:DetailRecordIntent):DetailRecordState{
        return when (intent){
            is DetailRecordIntent.initialize -> DetailRecordState()
            is DetailRecordIntent.SetUserRecord->{
                state.copy(
                    userRecord = intent.userRecord
                )
            }

            is DetailRecordIntent.SetBestPoseHistories -> {
                state.copy(
                    bestPoseRecords = intent.bestPosHistories
                )
            }
        }
    }
}