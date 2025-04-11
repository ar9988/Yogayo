package com.d104.domain.model

sealed class LoginResult {
    data class Success(
        val userId: Int,
        val userLoginId: String,
        val userName: String,
        val userNickname: String,
        val userProfile: String,
        val accessToken: String,
        val refreshToken: String
    ) : LoginResult()

    sealed class Error : LoginResult() {
        data class InvalidCredentials(val message: String) : Error()
        data class UserNotFound(val message: String) : Error()
    }
}