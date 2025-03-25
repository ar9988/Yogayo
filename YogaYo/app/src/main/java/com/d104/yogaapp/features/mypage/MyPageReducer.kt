package com.d104.yogaapp.features.mypage

import javax.inject.Inject

class MyPageReducer @Inject constructor(){
    fun reduce(currentState: MyPageState, intent: MyPageIntent): MyPageState {
        return when(intent){
            is MyPageIntent.Logout -> currentState.copy()
            is MyPageIntent.LogoutSuccess -> currentState.copy(
                isLogoutSuccessful = true
            )
        }
    }
}