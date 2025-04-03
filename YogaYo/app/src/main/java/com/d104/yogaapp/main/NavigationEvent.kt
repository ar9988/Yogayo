package com.d104.yogaapp.main

sealed class NavigationEvent {
    object NavigateToLogin : NavigationEvent()
    object NavigateToSignUp : NavigationEvent()
    // 필요한 다른 네비게이션 이벤트들...
}