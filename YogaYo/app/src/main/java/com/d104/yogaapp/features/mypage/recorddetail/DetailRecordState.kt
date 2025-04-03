package com.d104.yogaapp.features.mypage.recorddetail

import com.d104.domain.model.BestPoseRecord
import com.d104.domain.model.MyPageInfo

data class DetailRecordState (
    val isLoading: Boolean = false,
    val myPageInfo:MyPageInfo = MyPageInfo(
        userId = -1,
        userName = "",
        userNickName = "",
        userProfile = "",
        exDays = 0,
        exConDays = 0,
        roomWin = 0
    ),
    val bestPoseRecords: List<BestPoseRecord> = emptyList(),
)
