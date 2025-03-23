package com.d104.yogaapp.features.multi

import javax.inject.Inject

class MultiReducer @Inject constructor() {
    fun reduce(currentState: MultiState, intent: MultiIntent): MultiState {
        return when(intent) {
            is MultiIntent.CreateRoom -> currentState.copy(
                dialogState = DialogState.CREATING
            )
            is MultiIntent.SearchRoom -> currentState.copy(
                isLoading = true
            )
            is MultiIntent.SelectRoom -> currentState.copy(
                dialogState = DialogState.ENTERING
            )
            is MultiIntent.UpdateSearchText -> currentState.copy(
                searchText = intent.text
            )

            is MultiIntent.SelectCourse -> currentState.copy(
                course = intent.course
            )

            is MultiIntent.SearchCourse -> currentState.copy(
                dialogState = DialogState.SELECTING
            )

            is MultiIntent.NextPage -> currentState.copy(
                pageIndex = currentState.pageIndex+1
            )
            is MultiIntent.PrevPage -> currentState.copy(
                pageIndex = currentState.pageIndex-1
            )

            is MultiIntent.ClearRoom -> currentState.copy(
            )

            is MultiIntent.RoomLoaded -> currentState.copy(
                isLoading = false
            )
        }
    }
}