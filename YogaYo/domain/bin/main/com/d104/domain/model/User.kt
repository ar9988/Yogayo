package com.d104.domain.model

data class User(
    val userId: Int,
    val userLoginId: String,
    val userName: String,
    val userNickname: String,
    val userProfile: String,
)