package com.d104.domain.repository

import com.d104.domain.model.MyPageInfo
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun getMyPageInfo(): Flow<Result<MyPageInfo>>
}