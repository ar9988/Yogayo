package com.d104.yogaapp.features.multi.play

import javax.inject.Inject

class MultiPlayReducer @Inject constructor() {
    fun reduce(currentState: MultiPlayState, intent: MultiPlayIntent): MultiPlayState {
        return when(intent){
            is MultiPlayIntent.UserLeft -> currentState.copy()
            else -> currentState
        }
    }
}