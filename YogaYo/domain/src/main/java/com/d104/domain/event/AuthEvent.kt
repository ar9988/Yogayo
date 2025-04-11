package com.d104.domain.event

sealed class AuthEvent {
    data object TokenRefreshFailed : AuthEvent()
}