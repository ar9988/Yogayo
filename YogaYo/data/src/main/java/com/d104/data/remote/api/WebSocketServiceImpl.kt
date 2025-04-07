package com.d104.data.remote.api

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import javax.inject.Inject

class WebSocketServiceImpl @Inject constructor(
    private val client: OkHttpClient,
) : WebSocketService{
    // 현재 활성화된 웹소켓 인스턴스를 저장합니다.
    private var webSocket: WebSocket? = null
    // 웹소켓 연결 종료 시 사용할 표준 코드
    private val normalClosureStatus = 1000 // WebSocket 표준 정상 종료 코드

    override fun connect(url: String, listener: WebSocketListener) {
        Log.d("WebSocketService", ">>> connect() method entered. URL: $url") // 진입 확인용 로그 추가
        try {
            Log.d("WebSocketService", "Attempting disconnect() first...")
            disconnect() // 기존 연결 정리

            Log.d("WebSocketService", "Building request for URL: $url") // URL 형식 확인
            val request = Request.Builder()
                .url(url) // 여기서 IllegalArgumentException 발생 가능
                .build()
            Log.d("WebSocketService", "Request built successfully.")

            Log.d("WebSocketService", "Calling client.newWebSocket...")
            // 여기서 다양한 네트워크/보안 관련 예외 발생 가능
            webSocket = client.newWebSocket(request, listener)
            Log.d("WebSocketService", "client.newWebSocket call finished. WebSocket instance created: ${webSocket != null}")

            // 원래 로그 (여기까지 오면 성공)
            Log.d("WebSocketService", "웹소켓 연결 시도 완료 (Post-Init): $url")

        } catch (t: Throwable) {
            // 모든 종류의 예외를 잡아서 로그로 출력
            Log.e("WebSocketService", "!!! EXCEPTION occurred within connect() method !!!", t)
        }
    }

    override fun disconnect() {
        webSocket?.let {
            Log.d("WebSocketService", "웹소켓 연결 종료 시도.")
            // 정상 종료 코드(1000)와 함께 종료 메시지 전송 시도
            it.close(normalClosureStatus, "Client disconnected normally.")
            // webSocket 참조 제거
            webSocket = null
        } ?: run {
            Log.d("WebSocketService", "종료할 활성 웹소켓 연결 없음.")
        }
    }

    override fun send(message: String): Boolean {
        val sent = webSocket?.send(message) ?: false
        if (sent) {
            Log.d("WebSocketService", "메시지 전송 성공: $message")
        } else {
            Log.w("WebSocketService", "메시지 전송 실패 (연결되지 않음?): $message")
        }
        return sent
    }
}