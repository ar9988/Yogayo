package com.d104.yogaapp.features.multi

import com.d104.domain.model.Room
import com.d104.domain.model.UserCourse

sealed class MultiIntent {
    data class UpdateSearchText(val text:String) : MultiIntent()
    data object SearchRoom : MultiIntent()
    data class SelectRoom(val room: Room) : MultiIntent()
    data object SearchCourse : MultiIntent()
    data class SelectCourse(val course: UserCourse) : MultiIntent()
    data object CreateRoom : MultiIntent()
    data object PrevPage: MultiIntent()
    data object NextPage: MultiIntent()
    data object ClearRoom: MultiIntent()

}