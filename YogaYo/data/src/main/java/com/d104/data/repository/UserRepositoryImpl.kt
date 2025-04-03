package com.d104.data.repository

import com.d104.data.mapper.BadgeMapper
import com.d104.data.mapper.MyPageInfoMapper
import com.d104.data.remote.api.UserApiService
import com.d104.data.remote.datasource.user.UserDataSource
import com.d104.domain.model.Badge
import com.d104.domain.model.MyPageInfo
import com.d104.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userDataSource: UserDataSource,
    private val myPageInfoMapper: MyPageInfoMapper,
    private val badgeMapper: BadgeMapper
): UserRepository {
    override suspend fun getMyPageInfo(): Flow<Result<MyPageInfo>> = flow {
        try {
            val response = withContext(Dispatchers.IO) {
                userDataSource.getMyPageInfo()
            }
            val body = response.body()
            if (response.isSuccessful && body != null) {
                emit(Result.success(myPageInfoMapper.map(body)))
            } else {
                emit(Result.failure(IOException("API 호출 실패: ${response.code()} ${response.message()}")))
            }
        }catch (e:Exception){
            emit(Result.failure(e))
        }
    }

    override suspend fun getMyBadges(): Flow<Result<List<Badge>>> = flow{
        try {
            val response = withContext(Dispatchers.IO) {
                userDataSource.getMyBadges()
            }
            val body = response.body()
            if(response.isSuccessful && body != null){
                emit(Result.success(badgeMapper.mapToDomainList(body)))
            } else {
                emit(Result.failure(IOException("API 호출 실패: ${response.code()} ${response.message()}")))
            }
        }catch (e:Exception){
            emit(Result.failure(e))
        }
    }

    override suspend fun getNewBadges(): Flow<Result<List<Badge>>> =flow{
        try {
            val response = withContext(Dispatchers.IO) {
                userDataSource.getNewBadges()
            }
            val body = response.body()
            if (response.isSuccessful && body != null) {
                emit(Result.success(badgeMapper.mapToDomainList(body)))
            } else {
                emit(Result.failure(IOException("API 호출 실패: ${response.code()} ${response.message()}")))
            }
        }catch (e:Exception){
            emit(Result.failure(e))
        }
    }

}