package com.d104.data.remote.datasource.user

import com.d104.data.remote.api.UserApiService
import com.d104.data.remote.api.UserCourseApiService
import com.d104.data.remote.dto.BadgeResponseDto
import com.d104.data.remote.dto.MyPageInfoResponseDto
import retrofit2.Response
import javax.inject.Inject

class UserDataSourceImpl @Inject constructor(
    private val userApiService: UserApiService
): UserDataSource{
    override suspend fun getMyPageInfo(): Response<MyPageInfoResponseDto> = userApiService.getMyPageInfo()

    override suspend fun getMyBadges(): Response<List<BadgeResponseDto>> = userApiService.getMyBadges()
    override suspend fun getNewBadges(): Response<List<BadgeResponseDto>> = userApiService.getNewBadges()
}