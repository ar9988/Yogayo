package com.d104.data.repository

import com.d104.data.mapper.LoginMapper
import com.d104.data.remote.api.AuthApiService
import com.d104.data.remote.dto.LoginRequestDto
import com.d104.data.utils.ErrorUtils
import com.d104.domain.model.LoginResult
import com.d104.domain.model.User
import com.d104.domain.repository.AuthRepository
import com.d104.domain.repository.DataStoreRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApiService,
    private val dataStoreRepository: DataStoreRepository,
    private val loginMapper: LoginMapper,
) : AuthRepository {
    override suspend fun refreshAccessToken(refreshToken: String): String {
        TODO("Not yet implemented")
    }

    override suspend fun login(userId: String, password: String): Flow<Result<LoginResult>> {
        return flow {
            try {
                val loginResponseDto = authApi.login(
                    loginRequest = LoginRequestDto(
                        userId,
                        password
                    )
                )

                // 로그인 성공 시 토큰 저장
                dataStoreRepository.saveAccessToken(loginResponseDto.accessToken)
                dataStoreRepository.saveRefreshToken(loginResponseDto.refreshToken)
                dataStoreRepository.saveUser(
                    User(
                        userId = loginResponseDto.userId,
                        userLoginId = loginResponseDto.userLoginId,
                        userNickname = loginResponseDto.userNickname,
                        userProfile = loginResponseDto.userProfile,
                        userName = loginResponseDto.userName
                    )
                )
                // DTO를 도메인 모델로 변환하여 반환
                val loginResult = loginMapper.map(loginResponseDto)
                emit(Result.success(loginResult))

            } catch (e: HttpException) {
                val errorResult = when (e.code()) {
                    401 -> {
                        val errorBody = ErrorUtils.parseHttpError(e)
                        LoginResult.Error.InvalidCredentials(
                            errorBody?.error ?: "아이디 또는 비밀번호가 올바르지 않습니다."
                        )
                    }

                    404 -> {
                        val errorBody = ErrorUtils.parseHttpError(e)
                        LoginResult.Error.UserNotFound(
                            errorBody?.error ?: "사용자를 찾을 수 없습니다."
                        )
                    }

                    else -> {
                        // 서버 오류는 통신 실패로 간주
                        emit(Result.failure(e))
                        return@flow
                    }
                }
                // 401, 404는 통신은 성공했지만 로그인 실패로 간주
                emit(Result.success(errorResult))
            } catch (e: IOException) {
                // 네트워크 오류는 통신 실패로 간주
                emit(Result.failure(e))
            } catch (e: Exception) {
                // 기타 예외도 통신 실패로 간주
                emit(Result.failure(e))
            }
        }
    }
}