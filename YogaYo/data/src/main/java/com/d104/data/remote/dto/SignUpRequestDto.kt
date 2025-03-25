package com.d104.data.remote.dto

data class SignUpRequestDto(
    val userLoginId: String,
    val userPwd: String,
    val userName: String,
    val userNickname: String
)