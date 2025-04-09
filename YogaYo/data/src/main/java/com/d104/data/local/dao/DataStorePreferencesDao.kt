package com.d104.data.local.dao

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.d104.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataStorePreferencesDao @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : PreferencesDao {
    private val TAG = "PreferencesDao"

    companion object {
        // 모든 키를 여기서 집중 관리
        private val KEYS = object {
            val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
            val ACCESS_TOKEN = stringPreferencesKey("access_token")
            val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
            val USER_ID = stringPreferencesKey("user_id")
            val USER_LOGIN_ID = stringPreferencesKey("user_login_id")
            val USER_NAME = stringPreferencesKey("user_name")
            val USER_NICKNAME = stringPreferencesKey("user_nickname")
            val USER_PROFILE = stringPreferencesKey("user_profile")
        }
    }

    override suspend fun saveUser(user: User) {
        dataStore.edit { preferences ->
            preferences[KEYS.USER_ID] = user.userId.toString()
            preferences[KEYS.USER_LOGIN_ID] = user.userLoginId
            preferences[KEYS.USER_NAME] = user.userName
            preferences[KEYS.USER_NICKNAME] = user.userNickname
            preferences[KEYS.USER_PROFILE] = user.userProfile
            preferences[KEYS.IS_LOGGED_IN] = true
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
            val userId = preferences[KEYS.USER_ID]?.toIntOrNull()
            val userLoginId = preferences[KEYS.USER_LOGIN_ID]
            val userName = preferences[KEYS.USER_NAME]
            val userNickname = preferences[KEYS.USER_NICKNAME]
            val userProfile = preferences[KEYS.USER_PROFILE]

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

    override suspend fun clearUser() : Flow<Boolean> = flow {
        dataStore.edit { preferences ->
            preferences.remove(KEYS.USER_ID)
            preferences.remove(KEYS.USER_LOGIN_ID)
            preferences.remove(KEYS.USER_NAME)
            preferences.remove(KEYS.USER_NICKNAME)
            preferences.remove(KEYS.USER_PROFILE)
            preferences.remove(KEYS.ACCESS_TOKEN)
            preferences.remove(KEYS.REFRESH_TOKEN)
            preferences[KEYS.IS_LOGGED_IN] = false
        }
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
            preferences[KEYS.ACCESS_TOKEN]
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
            preferences[KEYS.REFRESH_TOKEN]
        }

    override suspend fun saveAccessToken(accessToken: String) {
        dataStore.edit { preferences ->
            preferences[KEYS.ACCESS_TOKEN] = accessToken
        }
    }

    override suspend fun saveRefreshToken(refreshToken: String) {
        dataStore.edit { preferences ->
            preferences[KEYS.REFRESH_TOKEN] = refreshToken
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
            preferences[KEYS.IS_LOGGED_IN] ?: false
        }

    override suspend fun getUserId(): String {
        return dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    Log.e(TAG, "Error reading preferences", exception)
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[KEYS.USER_ID] ?: ""
            }.first()
    }

    override suspend fun getUserName(): String {
        return dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    Log.e(TAG, "Error reading preferences", exception)
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[KEYS.USER_NAME] ?: ""
            }.first()
    }

    override suspend fun getUserIcon(): String {
        return dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    Log.e(TAG, "Error reading preferences", exception)
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[KEYS.USER_PROFILE] ?: ""
            }.first()
    }
}