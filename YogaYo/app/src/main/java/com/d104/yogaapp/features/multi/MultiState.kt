package com.d104.yogaapp.features.multi

import com.d104.domain.model.Room
import com.d104.domain.model.UserCourse
import com.d104.domain.model.YogaPose

data class MultiState(
    val selectedRoom: Room? = null,
    val isLoading: Boolean = false,
    val dialogState: DialogState = DialogState.NONE,
    val poseSearchTitle: String = "",
    val roomSearchText:String = "",
    val selectedCourse:UserCourse? = null,
    val roomTitle: String = "",
    val roomPassword: String = "",
    val pageIndex:Int = 0,
    var page: List<Room> = emptyList(),
    var poseList: List<YogaPose> = emptyList(),
    var yogaCourses: List<UserCourse> = emptyList(),
    var enteringRoom: Boolean = false,
    val roomMax: Int = 2,
    val isPassword: Boolean = false,
    val errorMessage: String? = null,
)
enum class DialogState {
    NONE, CREATING, ENTERING, COURSE_ADD
}