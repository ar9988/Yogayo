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

    override fun connect(url:String,listener: WebSocketListener) {
        disconnect() // 기존 연결 정리

        // 새 웹소켓 연결을 위한 요청 생성
        val request = Request.Builder()
            .url(url)
            .build()

        // OkHttpClient를 사용하여 새 웹소켓 생성 및 연결 시작
        // 실제 연결 과정 및 이벤트 처리는 OkHttp 내부와 제공된 listener에서 수행됩니다.
        webSocket = client.newWebSocket(request, listener)
        Log.d("WebSocketService", "웹소켓 연결 시도: $url")
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