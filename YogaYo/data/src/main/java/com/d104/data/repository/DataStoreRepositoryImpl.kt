package com.d104.data.repository

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.d104.domain.model.User
import com.d104.domain.repository.DataStoreRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DataStoreRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : DataStoreRepository {
    private val TAG = "UserPreferencesRepo"

    // 키 정의
    companion object {
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")

        // User 관련 키 추가
        val USER_ID = stringPreferencesKey("user_id")
        val USER_LOGIN_ID = stringPreferencesKey("user_login_id")
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_NICKNAME = stringPreferencesKey("user_nickname")
        val USER_PROFILE = stringPreferencesKey("user_profile")
    }


    override fun getAccessToken(): Flow<String?> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e(TAG, "Error reading preferences", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[ACCESS_TOKEN]
        }

    override fun getRefreshToken(): Flow<String?> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e(TAG, "Error reading preferences", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[REFRESH_TOKEN]
        }

    override suspend fun saveAccessToken(token: String) {
        TODO("Not yet implemented")
    }

    override suspend fun saveRefreshToken(token: String) {
        TODO("Not yet implemented")
    }

    override suspend fun saveUser(user: User) {
        dataStore.edit { preferences ->
            preferences[USER_ID] = user.userId.toString()
            preferences[USER_LOGIN_ID] = user.userLoginId
            preferences[USER_NAME] = user.userName
            preferences[USER_NICKNAME] = user.userNickname
            preferences[USER_PROFILE] = user.userProfile
            preferences[IS_LOGGED_IN] = true
        }
    }

    override fun getUser(): Flow<User?> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e(TAG, "Error reading user data", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val userId = preferences[USER_ID]?.toIntOrNull()
            val userLoginId = preferences[USER_LOGIN_ID]
            val userName = preferences[USER_NAME]
            val userNickname = preferences[USER_NICKNAME]
            val userProfile = preferences[USER_PROFILE]

            if (userId != null && userLoginId != null && userName != null &&
                userNickname != null && userProfile != null
            ) {
                User(
                    userId = userId,
                    userLoginId = userLoginId,
                    userName = userName,
                    userNickname = userNickname,
                    userProfile = userProfile
                )
            } else {
                null
            }
        }

    override fun isLoggedIn(): Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e(TAG, "Error reading preferences", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[IS_LOGGED_IN] ?: false
        }

    override suspend fun clearUserData() {
        dataStore.edit { preferences ->
            preferences.remove(USER_ID)
            preferences.remove(USER_LOGIN_ID)
            preferences.remove(USER_NAME)
            preferences.remove(USER_NICKNAME)
            preferences.remove(USER_PROFILE)
            preferences.remove(ACCESS_TOKEN)
            preferences.remove(REFRESH_TOKEN)
            preferences[IS_LOGGED_IN] = false
        }
    }

}