package com.d104.data.repository

import com.d104.data.remote.api.WebSocketService
import com.d104.domain.repository.WebSocketRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import javax.inject.Inject

class WebSocketRepositoryImpl @Inject constructor(
    private val webSocketService: WebSocketService
) : WebSocketRepository {
    override suspend fun connect(): Flow<String> = channelFlow {
        val listener = object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                trySend(text)
            }
            // 다른 콜백 메서드 구현...
        }
        val url = "ws://j12d104.p.ssafy.io"
        webSocketService.connect(url, listener)

        awaitClose {
            webSocketService.disconnect()
        }
    }

    override fun disconnect() {
        TODO("Not yet implemented")
    }

    override fun send(message: String) {
        TODO("Not yet implemented")
    }
}