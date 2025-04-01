package com.d104.yogaapp.main

import javax.inject.Inject

class MainReducer @Inject constructor() {
    fun reduce(currentState: MainState, intent: MainIntent): MainState {
        return when (intent) {
            is MainIntent.SelectTab ->
                currentState.copy(selectedTab = intent.tab)
            is MainIntent.SetBottomBarVisibility ->
                currentState.copy(showBottomBar = intent.visible)
            is MainIntent.SelectSoloCourse->
                currentState.copy(soloYogaCourse = intent.course)
            is MainIntent.ClearSoloCourse->
//                currentState.copy(soloYogaCourse = null)
                currentState

            is MainIntent.SetUserRecord -> {
                currentState.copy(userRecord = intent.userRecord)
            }
            is MainIntent.SelectRoom -> {
                currentState.copy(room = intent.room)
            }

            is MainIntent.ClearUserRecord -> {
                currentState.copy(userRecord = null)
            }
        }
    }
}