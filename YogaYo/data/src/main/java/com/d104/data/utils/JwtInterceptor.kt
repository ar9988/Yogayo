package com.d104.data.utils

import android.util.Log
import com.d104.domain.event.AuthEvent
import com.d104.domain.event.AuthEventManager
import com.d104.domain.usecase.GetAccessTokenUseCase
import com.d104.domain.usecase.GetRefreshTokenUseCase
import com.d104.domain.usecase.RefreshAccessTokenUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JwtInterceptor @Inject constructor(
    private val getAccessTokenUseCase: GetAccessTokenUseCase,// access token 요청
    private val refreshAccessTokenUseCase: RefreshAccessTokenUseCase, // refresh token으로 access token 갱신 요청
    private val getRefreshTokenUseCase: GetRefreshTokenUseCase, //refresh token 을 요청
    private val authEventManager : AuthEventManager
) : Interceptor {

    // 가장 최근 토큰을 저장하는 AtomicReference
    private val cachedToken = AtomicReference<String?>(null)

    // 토큰 갱신 중인지 확인하는 플래그
    private val isRefreshing = AtomicReference<Boolean>(false)

    // 코루틴 스코프
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    init {
        // 토큰 변경 관찰
        scope.launch {
            getAccessTokenUseCase().collect { token ->
                cachedToken.set(token)
                Log.d("JwtInterceptor", "Token updated: ${token != null}")
            }
        }
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = cachedToken.get()

        val requestWithJwt = if (token.isNullOrBlank()) {
            originalRequest
        } else {
            originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        }

        val response = chain.proceed(requestWithJwt)

        // 401 Unauthorized 처리
        if (response.code == 401 && !isRefreshing.get()) {
            response.close() // 원래 응답 닫기

            synchronized(this) {
                // 다른 스레드가 이미 토큰을 갱신했는지 확인
                val latestToken = cachedToken.get()
                if (latestToken != token) {
                    // 토큰이 이미 갱신되었다면 새 토큰으로 요청 재시도
                    val newRequest = originalRequest.newBuilder()
                        .header("Authorization", "Bearer $latestToken")
                        .build()
                    return chain.proceed(newRequest)
                }

                // 토큰 갱신 시작
                isRefreshing.set(true)

                try {
                    // 리프레시 토큰으로 새 액세스 토큰 요청
                    val newToken = refreshToken()

                    if (newToken != null) {
                        // 새 토큰으로 요청 재시도
                        val newRequest = originalRequest.newBuilder()
                            .header("Authorization", "Bearer $newToken")
                            .build()
                        return chain.proceed(newRequest)
                    }
                } finally {
                    isRefreshing.set(false)
                }
            }
        }

        return response
    }

    private fun refreshToken(): String? {
        // 동기적으로 토큰 갱신 처리
        return try {
            // runBlocking을 사용하여 코루틴 내에서 동기적으로 실행
            kotlinx.coroutines.runBlocking(Dispatchers.IO) {
                val refreshToken = getRefreshTokenUseCase().first()
                val result = refreshAccessTokenUseCase.execute(refreshToken)
                if (result.isSuccess) {
                    // 새 토큰 반환
                    result.getOrNull()
                } else {
                    // 토큰 갱신 실패 시 로그아웃 처리 등을 수행할 수 있음
                    Log.e("JwtInterceptor", "Token refresh failed", result.exceptionOrNull())
                    authEventManager.emitAuthEvent(AuthEvent.TokenRefreshFailed)
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("JwtInterceptor", "Exception during token refresh", e)
            kotlinx.coroutines.runBlocking {
                authEventManager.emitAuthEvent(AuthEvent.TokenRefreshFailed)
            }
            null
        }
    }
}
