package com.d104.yogaapp.main

import com.d104.domain.model.UserCourse

data class MainState(
    val selectedTab: Tab = Tab.Solo,
    val showBottomBar: Boolean = true,
    val soloYogaCourse:UserCourse? = null
)
enum class Tab{
    Solo, Multi, MyPage
}
