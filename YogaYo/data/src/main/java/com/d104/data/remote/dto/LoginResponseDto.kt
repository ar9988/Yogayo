package com.d104.data.remote.dto

import com.google.gson.annotations.SerializedName

data class LoginResponseDto(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("user_login_id") val userLoginId: String,
    @SerializedName("user_name") val userName: String,
    @SerializedName("user_nickname") val userNickname: String,
    @SerializedName("user_profile") val userProfile: String,
    @SerializedName("accessToken") val accessToken: String,
    @SerializedName("refreshToken") val refreshToken: String
)