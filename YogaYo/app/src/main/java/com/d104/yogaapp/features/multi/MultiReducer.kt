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

            is MultiIntent.UpdateRoomPasswordChecked -> currentState.copy(
                isPassword = intent.it
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
            is MultiIntent.EnterRoomSuccess -> currentState.copy(
                enteringRoom = true
            )
            is MultiIntent.EnterRoomComplete -> currentState.copy(
                enteringRoom = false
            )
            is MultiIntent.EnterRoom -> currentState.copy(
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

            is MultiIntent.UpdateCourse -> {
                // 1. 현재 상태에 있는 코스들의 ID를 Set으로 만들어 효율적인 중복 체크 준비
                val existingCourseIds = currentState.yogaCourses.map { it.courseId }.toSet()

                // 2. 새로 들어온 코스 리스트(intent.it)에서
                //    현재 상태에 없는 ID를 가진 코스들만 필터링
                val newCoursesToAdd = intent.it.filter { newCourse ->
                    newCourse.courseId !in existingCourseIds
                }

                // 3. 새로 추가할 코스가 있을 경우에만 상태 업데이트
                if (newCoursesToAdd.isNotEmpty()) {
                    // 기존 리스트와 중복되지 않는 새 코스 리스트를 합쳐서 새로운 리스트 생성
                    val updatedYogaCourses = currentState.yogaCourses + newCoursesToAdd
                    currentState.copy(yogaCourses = updatedYogaCourses)
                } else {
                    // 추가할 새 코스가 없으면 상태를 변경하지 않고 그대로 반환 (불필요한 리컴포지션 방지)
                    currentState
                }
            }
        }
    }
}
