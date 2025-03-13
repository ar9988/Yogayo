package com.d104.yogaapp.main

import javax.inject.Inject

class MainReducer @Inject constructor() {
    fun reduce(currentState: MainState, intent: MainIntent): MainState {
        return when (intent) {
            is MainIntent.SelectTab -> currentState.copy(selectedTab = intent.tab)
        }
    }
}