package com.d104.yogaapp.main

import com.d104.domain.model.UserCourse

sealed class MainIntent {
    data class SelectTab(val tab: Tab) : MainIntent()
    data class SetBottomBarVisibility(val visible: Boolean) : MainIntent()

    data class SelectSoloCourse(val course: UserCourse) : MainIntent()
    object ClearSoloCourse:MainIntent()

}