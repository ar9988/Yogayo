package com.d104.yogaapp.main

data class MainState(
    val selectedTab: Tab = Tab.Solo,
    val showBottomBar: Boolean = true
)
enum class Tab{
    Solo, Multi, MyPage
}
