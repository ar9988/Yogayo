package com.d104.data.remote.api

import com.d104.data.remote.dto.UserInfoDto
import retrofit2.http.GET

interface UserApiService {
    @GET("user/info")
    suspend fun getUserInfo(): UserInfoDto
}