package com.d104.yogaapp.features.multi

import javax.inject.Inject

class MultiReducer @Inject constructor() {
    fun reduce(currentState: MultiState, intent: MultiIntent): MultiState {
        return when (intent) {
            is MultiIntent.ClickCreateRoomButton -> currentState.copy(
                dialogState = DialogState.CREATING,
                selectedCourse = null,
                roomTitle = "",
                roomPassword = "",
                roomMax = 2,
                isPassword = false


            )

            is MultiIntent.SearchRoom -> currentState.copy(
                isLoading = true
            )

            is MultiIntent.SelectRoom -> currentState.copy(
                dialogState = DialogState.ENTERING,
                selectedRoom = intent.room
            )

            is MultiIntent.UpdateSearchText -> currentState.copy(
                roomSearchText = intent.text
            )

            is MultiIntent.SelectCourse -> {
                currentState.copy(
                    selectedCourse = intent.course
                )
            }

            is MultiIntent.SearchCourse -> currentState

            is MultiIntent.NextPage -> currentState.copy(
                pageIndex = currentState.pageIndex + 1
            )

            is MultiIntent.PrevPage -> {
                if(currentState.pageIndex>0){
                    currentState.copy(
                        pageIndex = currentState.pageIndex - 1
                    )
                } else{
                    currentState
                }
            }

            is MultiIntent.ClearRoom -> currentState.copy(
                selectedRoom = null
            )

            is MultiIntent.RoomLoaded -> currentState.copy(
                isLoading = false
            )

            is MultiIntent.DismissDialog -> {
                when(intent.dialogState){
                    DialogState.NONE-> currentState
                    DialogState.CREATING -> currentState.copy(
                        dialogState = DialogState.NONE
                    )
                    DialogState.ENTERING -> currentState.copy(
                        dialogState = DialogState.NONE
                    )
                    DialogState.COURSE_EDITING -> currentState.copy(
                        dialogState = DialogState.CREATING
                    )
                }
            }
            is MultiIntent.UpdateRoomPassword -> currentState.copy(
                roomPassword = intent.password
            )
            is MultiIntent.UpdateRoomTitle -> currentState.copy(
                roomTitle = intent.title
            )

            is MultiIntent.UpdatePoseTitle -> currentState.copy(
                poseSearchTitle = intent.title
            )

            is MultiIntent.EditCourse -> {
                currentState
            }

            is MultiIntent.ShowEditDialog -> currentState.copy(
                dialogState = DialogState.COURSE_EDITING
            )
            is MultiIntent.EnterRoomComplete -> currentState.copy(
                enteringRoom = false
            )
            is MultiIntent.EnterRoom -> currentState.copy(
                enteringRoom = true,
                dialogState = DialogState.NONE
            )

            is MultiIntent.EnterRoomFail -> currentState.copy(
                enteringRoom = false,
                errorMessage = intent.message
            )

            is MultiIntent.CreateRoomFail -> currentState.copy(
                errorMessage = intent.message
            )

            is MultiIntent.CreateRoom -> currentState.copy(

            )

            is MultiIntent.UpdatePage -> currentState.copy(
                page = intent.it
            )

            is MultiIntent.ClearErrorMessage -> currentState.copy(
                errorMessage = null
            )

            is MultiIntent.UpdateRoomMaxCount -> currentState.copy(
                roomMax = intent.maxCount
            )
        }
    }
}
