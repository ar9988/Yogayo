package com.d104.yogaapp.features.multi

import com.d104.domain.model.Room
import com.d104.domain.model.UserCourse
import com.d104.domain.model.YogaPoseWithOrder

sealed class MultiIntent {
    data class UpdateSearchText(val text:String) : MultiIntent()
    data object SearchRoom : MultiIntent()
    data class SelectRoom(val room: Room) : MultiIntent()
    data object SearchCourse : MultiIntent()
    data class SelectCourse(val course: UserCourse) : MultiIntent()
    data object ClickCreateRoomButton : MultiIntent()
    data object PrevPage: MultiIntent()
    data object NextPage: MultiIntent()
    data object ClearRoom: MultiIntent()
    data object RoomLoaded: MultiIntent()
    data object ShowEditDialog:MultiIntent()
    data class UpdateRoomTitle(val title:String) : MultiIntent()
    data class UpdateRoomPassword(val password:String) : MultiIntent()
    data class UpdatePoseTitle(val title:String): MultiIntent()
    data class DismissDialog(val dialogState: DialogState): MultiIntent()
    data class EditCourse(val courseId:Long,val courseName: String, val poses: List<YogaPoseWithOrder>) : MultiIntent()
    data object EnterRoom: MultiIntent()
    data object EnterRoomComplete: MultiIntent()
    data class EnterRoomFail(val message:String) : MultiIntent()
    data class CreateRoomFail(val message: String) : MultiIntent()
    data class UpdatePage(val it: List<Room>) : MultiIntent()

    data object CreateRoom : MultiIntent()

    data object ClearErrorMessage: MultiIntent()
}