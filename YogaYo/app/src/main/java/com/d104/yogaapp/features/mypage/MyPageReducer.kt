package com.d104.yogaapp.features.mypage

import javax.inject.Inject

class MyPageReducer @Inject constructor(){
    fun reduce(currentState: MyPageState, intent: MyPageIntent): MyPageState {
        return when(intent){
            MyPageIntent.Initialize -> currentState
            MyPageIntent.ShowNextBadge -> {
                val nextIndex = currentState.currentNewBadgeIndex + 1
                if (nextIndex < currentState.newBadgeList.size) {
                    currentState.copy(currentNewBadgeIndex = nextIndex)
                } else {
                    currentState.copy(showBadgeOverlay = false, currentNewBadgeIndex = 0)
                }
            }
            MyPageIntent.CloseBadgeOverlay -> currentState.copy(showBadgeOverlay = false, currentNewBadgeIndex = 0)
            is MyPageIntent.Logout -> currentState.copy()
            is MyPageIntent.LogoutSuccess -> currentState.copy(
                isLogoutSuccessful = true
            )
            is MyPageIntent.SetMyPageInfo -> currentState.copy(myPageInfo = intent.myPageInfo)
            is MyPageIntent.SetMyBadges -> currentState.copy(myBadgeList = intent.myBadgeList)

            is MyPageIntent.SetNewBadges -> {
                if (intent.newBadges.isNotEmpty()) {
                    currentState.copy(
                        newBadgeList = intent.newBadges,
                        showBadgeOverlay = true,
                        currentNewBadgeIndex = 0
                    )
                } else {
                    currentState
                }
            }

        }
    }
}