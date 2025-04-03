package com.d104.yogaapp.main

import com.d104.domain.model.Room
import com.d104.domain.model.UserCourse
import com.d104.domain.model.MyPageInfo

data class MainState(
    val selectedTab: Tab = Tab.Solo,
    val showBottomBar: Boolean = true,
    val soloYogaCourse:UserCourse? = null,
    val myPageInfo: MyPageInfo? = null,
    val isLogin:Boolean = false,
    val room: Room? = null
)
enum class Tab{
    Solo, Multi, MyPage
}
