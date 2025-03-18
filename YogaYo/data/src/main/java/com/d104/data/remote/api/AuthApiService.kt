package com.d104.data.remote.api

import com.d104.data.remote.dto.LoginRequestDto
import com.d104.data.remote.dto.LoginResponseDto
import com.d104.data.remote.dto.SignUpResponseDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface AuthApiService {
    @POST("api/auth/login")
    suspend fun login(
        @Body loginRequest: LoginRequestDto
    ): LoginResponseDto

    @Multipart
    @POST("api/auth/signup")
    suspend fun signup(
        @Part("userLoginId") userLoginId:RequestBody,
        @Part("userPwd") userPwd:RequestBody,
        @Part("userName") userName: RequestBody,
        @Part("userNickname") userNickname: RequestBody,
        @Part userProfile:MultipartBody.Part
    ): SignUpResponseDto
}