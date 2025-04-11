package com.d104.domain.repository

import com.d104.domain.model.LoginResult
import com.d104.domain.model.SignUpResult
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun refreshAccessToken(refreshToken: String): String

    suspend fun getUserId(): String

    suspend fun login(userId:String, password:String) : Flow<Result<LoginResult>>

    suspend fun signUp(
        id: String,
        password: String,
        name: String,
        nickName: String,
        profileUri: String
    ):Flow<Result<SignUpResult>>

    suspend fun getUserName(): String
    suspend fun getUserIcon(): String
}