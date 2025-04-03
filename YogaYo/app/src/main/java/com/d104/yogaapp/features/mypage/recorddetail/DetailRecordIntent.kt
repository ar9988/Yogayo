package com.d104.yogaapp.features.mypage.recorddetail

import com.d104.domain.model.BestPoseRecord
import com.d104.domain.model.MyPageInfo

sealed class DetailRecordIntent {
    object initialize : DetailRecordIntent()
    data class  SetUserRecord(val myPageInfo: MyPageInfo):DetailRecordIntent()
    data class  SetBestPoseHistories(val bestPosHistories: List<BestPoseRecord>):DetailRecordIntent()





}