package com.d104.domain.repository

import kotlinx.coroutines.flow.Flow

interface DataStoreRepository {
    fun getAccessToken(): Flow<String?>
    fun getRefreshToken(): Flow<String?>
    suspend fun saveAccessToken(token:String)
    suspend fun saveRefreshToken(token:String)
}