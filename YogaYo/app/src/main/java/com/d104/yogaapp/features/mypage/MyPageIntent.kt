package com.d104.yogaapp.features.mypage

import com.d104.domain.model.Badge
import com.d104.domain.model.MyPageInfo

sealed class MyPageIntent {
    data object Logout : MyPageIntent()
    data object LogoutSuccess: MyPageIntent()
    data object Initialize : MyPageIntent()
    data object ShowNextBadge : MyPageIntent()
    data object CloseBadgeOverlay : MyPageIntent()
    data class SetMyPageInfo(val myPageInfo: MyPageInfo) : MyPageIntent()
    data class SetMyBadges(val myBadgeList : List<Badge>) : MyPageIntent()
    data class SetNewBadges(val newBadges: List<Badge>) : MyPageIntent()


}