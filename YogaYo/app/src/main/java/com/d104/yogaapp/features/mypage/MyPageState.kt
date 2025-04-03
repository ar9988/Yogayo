package com.d104.yogaapp.features.mypage

import com.d104.domain.model.Badge
import com.d104.domain.model.MyPageInfo

data class MyPageState(
    val isLoading: Boolean = false,
    val isLogoutSuccessful:Boolean = false,
    val myBadgeList:List<Badge> = emptyList(),
    val myPageInfo:MyPageInfo = MyPageInfo(
        userId = -1,
        userName = "",
        userNickName = "",
        userProfile = "",
        exDays = 0,
        exConDays = 0,
        roomWin = 0
    ),
    val newBadgeList: List<Badge> = emptyList(), // 새로 추가: 새로 획득한 뱃지 목록
    val currentNewBadgeIndex: Int = 0, // 새로 추가: 현재 보여주는 뱃지 인덱스
    val showBadgeOverlay: Boolean = false,
)