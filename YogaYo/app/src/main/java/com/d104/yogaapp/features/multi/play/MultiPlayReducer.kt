package com.d104.yogaapp.features.multi.play

import javax.inject.Inject

class MultiPlayReducer @Inject constructor() {
    fun reduce(currentState: MultiPlayState, intent: MultiPlayIntent): MultiPlayState {
        return when(intent){
            is MultiPlayIntent.UserLeft -> currentState.copy()
            is MultiPlayIntent.UpdateCameraPermission -> currentState.copy(
                cameraPermissionGranted = intent.granted
            )
            is MultiPlayIntent.UserJoined -> currentState.copy()
            is MultiPlayIntent.ClickMenu -> currentState.copy(
                menuClicked = !currentState.menuClicked
            )
            is MultiPlayIntent.ExitRoom -> currentState.copy(
                menuClicked = !currentState.menuClicked
            )
            else -> currentState
        }
    }
}