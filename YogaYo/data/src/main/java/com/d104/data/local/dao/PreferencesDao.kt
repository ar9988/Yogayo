package com.d104.data.local.dao

import com.d104.domain.model.User
import kotlinx.coroutines.flow.Flow

interface PreferencesDao {
    // 사용자 관련
    suspend fun saveUser(user: User)
    fun getUser(): Flow<User?>
    suspend fun clearUser() : Flow<Boolean>

    // 토큰 관련
    fun getAccessToken(): Flow<String?>
    fun getRefreshToken(): Flow<String?>
    suspend fun saveAccessToken(accessToken: String)
    suspend fun saveRefreshToken(refreshToken: String)

    // 인증 상태
    fun isLoggedIn(): Flow<Boolean>
    suspend fun getUserId(): String
    suspend fun getUserName(): String
}