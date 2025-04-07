package com.d104.domain.repository

import com.d104.domain.model.Badge
import com.d104.domain.model.MyPageInfo
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun getMyPageInfo(): Flow<Result<MyPageInfo>>
    suspend fun getMyBadges(): Flow<Result<List<Badge>>>
    suspend fun getNewBadges(): Flow<Result<List<Badge>>>
}