package com.d104.yogaapp.features.mypage

sealed class MyPageIntent {
    data object Logout : MyPageIntent()
    data object LogoutSuccess: MyPageIntent()
}