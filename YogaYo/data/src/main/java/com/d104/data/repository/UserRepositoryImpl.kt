package com.d104.data.repository

import com.d104.data.mapper.MyPageInfoMapper
import com.d104.data.remote.api.UserApiService
import com.d104.domain.model.MyPageInfo
import com.d104.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userApi: UserApiService,
    private val myPageInfoMapper: MyPageInfoMapper
): UserRepository {
    override suspend fun getMyPageInfo(): Flow<Result<MyPageInfo>> {
        return flow {
            try {
                val myPageInfoDto = userApi.getMyPageInfo()
                val myPageInfo = myPageInfoMapper.map(myPageInfoDto)
                emit(Result.success(myPageInfo))
            } catch (e:Exception){
                emit(Result.failure(e))
            }
        }
    }
}