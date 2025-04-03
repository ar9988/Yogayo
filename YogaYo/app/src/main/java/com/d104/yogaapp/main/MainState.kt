package com.d104.yogaapp.main

import com.d104.domain.model.Room
import com.d104.domain.model.UserCourse
import com.d104.domain.model.UserRecord
import com.d104.domain.model.MyPageInfo

data class MainState(
    val selectedTab: Tab = Tab.Solo,
    val showBottomBar: Boolean = true,
    val soloYogaCourse:UserCourse? = null,
    val userRecord: UserRecord = UserRecord(
        userId = -1,
        userName = "",
        userNickName = "",
        userProfile = "",
        exDays = 0,
        exConDays = 0,
        roomWin = 0
    ),
    val isLogin:Boolean = false,
    val myPageInfo: MyPageInfo? = null,
    val room: Room? = null
)
enum class Tab{
    Solo, Multi, MyPage
}
