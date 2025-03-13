package com.d104.yogaapp.main

data class MainState(
    val selectedTab: Tab = Tab.Solo
)
enum class Tab{
    Solo, Multi, MyPage
}
