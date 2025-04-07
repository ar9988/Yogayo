package com.d104.data.repository

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.d104.data.mapper.LoginMapper
import com.d104.data.mapper.SignUpMapper
import com.d104.data.remote.api.AuthApiService
import com.d104.data.remote.dto.LoginRequestDto
import com.d104.data.remote.dto.SignUpRequestDto
import com.d104.data.utils.ErrorUtils
import com.d104.domain.model.LoginResult
import com.d104.domain.model.SignUpResult
import com.d104.domain.model.User
import com.d104.domain.repository.AuthRepository
import com.d104.domain.repository.DataStoreRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.HttpException
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authApi: AuthApiService,
    private val dataStoreRepository: DataStoreRepository,
    private val loginMapper: LoginMapper,
    private val signUpMapper: SignUpMapper
) : AuthRepository {
    override suspend fun refreshAccessToken(refreshToken: String): String {
        return ""
    }

    override suspend fun getUserId(): String {
        return dataStoreRepository.getUserId()
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
                val profilePart: MultipartBody.Part? = if (profileUri.isNotBlank()) {
                    createMultipartBodyPartFromUri(context, profileUri, "userProfile")
                } else {
                    null
                }

                println("Final Profile Part: $profilePart") // 최종 결과 확인용

                val signUpResponseDto = authApi.signup(
                    signUpRequest = SignUpRequestDto(
                        id,
                        password,
                        name,
                        nickName
                    ),
                    userProfile = profilePart
                )
                // DTO를 도메인 모델로 변환하여 반환
                val signUpResult = signUpMapper.map(signUpResponseDto)
                emit(Result.success(signUpResult))

            } catch (e: HttpException) {
                val errorResult = when (e.code()) {
                    400 -> SignUpResult.Error.BadRequest(ErrorUtils.parseHttpError(e)?.error ?: "옳바르지 않은 입력 형식입니다")
                    409 -> SignUpResult.Error.ConflictUser(ErrorUtils.parseHttpError(e)?.error ?: "이미 사용중인 유저아이디 입니다.")
                    else -> {
                        emit(Result.failure(e))
                        return@flow
                    }
                }
                emit(Result.success(errorResult))
            } catch (e: IOException) {
                println("IOException during sign up: ${e.message}") // IO 예외 로그 추가
                emit(Result.failure(e))
            } catch (e: Exception) {
                println("General Exception during sign up: ${e.message}") // 일반 예외 로그 추가
                e.printStackTrace()
                emit(Result.failure(e))
            }
        }
    }

    // Content URI를 처리하여 MultipartBody.Part 생성하는 헬퍼 함수
    private fun createMultipartBodyPartFromUri(context: Context, uriString: String, partName: String): MultipartBody.Part? {
        val contentResolver: ContentResolver = context.contentResolver
        val uri = Uri.parse(uriString)
        var inputStream: InputStream? = null
        var tempFile: File? = null

        try {
            inputStream = contentResolver.openInputStream(uri) ?: return null // 스트림 열기

            // MIME 타입 가져오기
            val mimeType = contentResolver.getType(uri) ?: "image/*" // 타입 모르면 기본값 사용
            val mediaType = mimeType.toMediaTypeOrNull()

            // 파일 이름 가져오기 (선택적이지만 권장)
            val fileName = getFileNameFromUri(contentResolver, uri) ?: "profile_${System.currentTimeMillis()}" // 이름 모르면 임시 이름 생성

            // 임시 파일 생성
            tempFile = File.createTempFile("upload_", fileName.substringAfterLast('.'), context.cacheDir)
            val outputStream = FileOutputStream(tempFile)

            // InputStream 내용을 임시 파일로 복사
            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }

            // 임시 파일로부터 RequestBody 생성
            val requestBody = tempFile.asRequestBody(mediaType)

            // MultipartBody.Part 생성
            return MultipartBody.Part.createFormData(partName, fileName, requestBody)

        } catch (e: Exception) {
            println("Error creating multipart part from URI: ${e.message}")
            e.printStackTrace()
            return null // 오류 발생 시 null 반환
        } finally {
            // 사용한 리소스 정리
            try {
                inputStream?.close()
            } catch (e: IOException) { /* 무시 */ }
            // tempFile?.delete() // OkHttp가 RequestBody를 다 사용한 후 삭제하는 것이 더 안전할 수 있음.
            // 혹은 API 호출 후 finally 블록에서 삭제 고려. 일단 생성 후 반환.
            // 앱 종료 시 캐시 디렉토리는 정리될 수 있음.
        }
    }

    // Content URI에서 파일 이름을 가져오는 헬퍼 함수 (모든 경우에 동작하지 않을 수 있음)
    private fun getFileNameFromUri(contentResolver: ContentResolver, uri: Uri): String? {
        var fileName: String? = null
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (displayNameIndex != -1) {
                    fileName = cursor.getString(displayNameIndex)
                }
            }
        }
        return fileName
    }
}
