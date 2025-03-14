package com.d104.domain.repository

import com.d104.domain.model.UserInfo
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun getUserInfo(): Flow<UserInfo>
}