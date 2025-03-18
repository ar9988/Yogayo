package com.d104.data.remote.api

import com.d104.data.remote.dto.MyPageInfoDto
import retrofit2.http.GET

interface UserApiService {
    @GET("api/user/mypage")
    suspend fun getMyPageInfo(): MyPageInfoDto
}