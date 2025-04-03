package com.d104.data.remote.api

import com.d104.data.remote.dto.BadgeResponseDto
import com.d104.data.remote.dto.MyPageInfoResponseDto
import com.d104.domain.model.Badge
import retrofit2.Response
import retrofit2.http.GET

interface UserApiService {
    @GET("api/user/info")
    suspend fun getMyPageInfo(): Response<MyPageInfoResponseDto>

    @GET("api/user/badge")
    suspend fun getMyBadges(): Response<List<BadgeResponseDto>>

    @GET("api/user/badge/check")
    suspend fun getNewBadges():Response<List<BadgeResponseDto>>
}