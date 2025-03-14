package com.d104.data.repository

import com.d104.data.mapper.UserInfoMapper
import com.d104.data.remote.api.UserApiService
import com.d104.domain.model.UserInfo
import com.d104.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userApi: UserApiService,
    private val userInfoMapper: UserInfoMapper
): UserRepository {
    override suspend fun getUserInfo(): Flow<UserInfo> {
        return flow {
            val userInfoDto = userApi.getUserInfo() // DTO 반환
            val userInfo = userInfoMapper.map(userInfoDto) // Domain 모델로 변환
            emit(userInfo)
        }
    }
}