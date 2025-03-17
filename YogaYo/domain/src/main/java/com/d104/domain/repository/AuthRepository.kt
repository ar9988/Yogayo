package com.d104.domain.repository

import com.d104.domain.model.LoginResult
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun refreshAccessToken(refreshToken: String): String

    suspend fun login(userId:String, password:String) : Flow<Result<LoginResult>>
}