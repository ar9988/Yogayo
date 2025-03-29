package com.d104.domain.usecase

import com.d104.domain.repository.AuthRepository
import com.d104.domain.repository.DataStoreRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class RefreshAccessTokenUseCase @Inject constructor(
    private val dataStoreRepository: DataStoreRepository,
    private val authRepository: AuthRepository
) {
    suspend fun execute(refreshToken: String?): Result<String> {
        if (refreshToken == null) {
            return Result.failure(Exception("refreshToken cannot be null"))
        }

        return try {
            // 서버에 토큰 갱신 요청
            val accessToken = authRepository.refreshAccessToken(refreshToken)

            // 새 액세스 토큰 저장
            dataStoreRepository.saveAccessToken(accessToken)
            Result.success(accessToken)
        } catch (e: Exception) {
            // 예외 로깅 추가 (선택 사항)
            Result.failure(e)
        }
    }
}