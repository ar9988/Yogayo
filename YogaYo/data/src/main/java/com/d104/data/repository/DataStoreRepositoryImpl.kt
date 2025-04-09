package com.d104.data.repository

import com.d104.data.local.dao.PreferencesDao
import com.d104.domain.model.User
import com.d104.domain.repository.DataStoreRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DataStoreRepositoryImpl @Inject constructor(
    private val preferencesDao: PreferencesDao
) : DataStoreRepository {

    override fun getAccessToken(): Flow<String?> = preferencesDao.getAccessToken()

    override fun getRefreshToken(): Flow<String?> = preferencesDao.getRefreshToken()

    override suspend fun saveAccessToken(token: String) {
        preferencesDao.saveAccessToken(token)
    }

    override suspend fun saveRefreshToken(token: String) {
        preferencesDao.saveRefreshToken(token)
    }

    override suspend fun saveUser(user: User) {
        preferencesDao.saveUser(user)
    }

    override fun getUser(): Flow<User?> = preferencesDao.getUser()

    override fun isLoggedIn(): Flow<Boolean> = preferencesDao.isLoggedIn()

    override suspend fun clearUserData(): Flow<Boolean> {
        return preferencesDao.clearUser()
    }

    override suspend fun getUserId(): String {
        return preferencesDao.getUserId()
    }

    override suspend fun getUserName(): String {
        return preferencesDao.getUserName()
    }

    override suspend fun getUserIcon(): String {
        return preferencesDao.getUserIcon()
    }

}