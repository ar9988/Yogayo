package com.d104.data.remote.datasource.user

import com.d104.data.remote.dto.BadgeResponseDto
import com.d104.data.remote.dto.MyPageInfoResponseDto
import retrofit2.Response

interface UserDataSource {
    suspend fun getMyPageInfo(): Response<MyPageInfoResponseDto>
    suspend fun getMyBadges(): Response<List<BadgeResponseDto>>
    suspend fun getNewBadges():Response<List<BadgeResponseDto>>
}