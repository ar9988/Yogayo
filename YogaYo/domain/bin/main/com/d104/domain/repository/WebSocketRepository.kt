package com.d104.domain.repository

import com.d104.domain.utils.StompConnectionState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface WebSocketRepository {
    // 연결 상태 Flow 제공 (유지)
    val connectionState: StateFlow<StompConnectionState>
    suspend fun connect(topic: String) : Flow<String>
    fun disconnect()
    fun send(destination:String, message: String) : Boolean
}