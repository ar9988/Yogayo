package com.d104.domain.repository

import com.d104.domain.model.User
import kotlinx.coroutines.flow.Flow

interface DataStoreRepository {
    fun getAccessToken(): Flow<String?>
    fun getRefreshToken(): Flow<String?>
    fun getUser(): Flow<User?>
    fun isLoggedIn(): Flow<Boolean>
    suspend fun saveAccessToken(token:String)
    suspend fun saveRefreshToken(token:String)
    suspend fun saveUser(user: User)
    suspend fun clearUserData() : Flow<Boolean>
    suspend fun getUserId(): String
    suspend fun getUserName(): String

}