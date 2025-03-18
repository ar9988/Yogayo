package com.d104.data.repository

import com.d104.data.mapper.LoginMapper
import com.d104.data.mapper.SignUpMapper
import com.d104.data.remote.api.AuthApiService
import com.d104.data.remote.dto.LoginRequestDto
import com.d104.data.utils.ErrorUtils
import com.d104.domain.model.LoginResult
import com.d104.domain.model.SignUpResult
import com.d104.domain.model.User
import com.d104.domain.repository.AuthRepository
import com.d104.domain.repository.DataStoreRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.File
import java.io.IOException
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApiService,
    private val dataStoreRepository: DataStoreRepository,
    private val loginMapper: LoginMapper,
    private val signUpMapper: SignUpMapper
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

    override suspend fun signUp(
        id: String,
        password: String,
        name: String,
        nickName: String,
        profileUri: String
    ): Flow<Result<SignUpResult>> {
        return flow {
            try {
                val idRequestBody = id.toRequestBody("text/plain".toMediaTypeOrNull())
                val passwordRequestBody = password.toRequestBody("text/plain".toMediaTypeOrNull())
                val nameRequestBody = name.toRequestBody("text/plain".toMediaTypeOrNull())
                val nickNameRequestBody = nickName.toRequestBody("text/plain".toMediaTypeOrNull())

                // profileUri를 File로 변환 후 MultipartBody.Part로 생성
                val file = File(profileUri)
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                val profilePart =
                    MultipartBody.Part.createFormData("userProfile", file.name, requestFile)

                val signUpResponseDto = authApi.signup(
                    userLoginId = idRequestBody,
                    userPwd = passwordRequestBody,
                    userName = nameRequestBody,
                    userNickname = nickNameRequestBody,
                    userProfile = profilePart
                )
                // DTO를 도메인 모델로 변환하여 반환
                val signUpResult = signUpMapper.map(signUpResponseDto)
                emit(Result.success(signUpResult))

            } catch (e: HttpException) {
                emit(Result.failure(e))
                return@flow

            } catch (e: IOException) {
                // 네트워크 오류
                emit(Result.failure(e))

            } catch (e: Exception) {
                // 기타 예외
                emit(Result.failure(e))
            }
        }
    }
}