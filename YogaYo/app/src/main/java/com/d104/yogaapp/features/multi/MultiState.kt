package com.d104.yogaapp.features.multi

import com.d104.domain.model.Room
import com.d104.domain.model.UserCourse

data class MultiState(
    val selectedRoom: Room? = null,
    val isLoading: Boolean = false,
    val dialogState: DialogState = DialogState.NONE,
    val searchText:String = "",
    val course:UserCourse? = null,
    val roomTitle: String = "",
    val roomPassword: String = "",
    val pageIndex:Int = 0,
    var page: List<Room> = emptyList()
)

enum class DialogState {
    NONE, CREATING, ENTERING, SELECTING
}