package com.d104.domain.repository

import com.d104.domain.model.SignalingMessage
import com.d104.domain.model.WebRTCConnectionState
import kotlinx.coroutines.flow.Flow

interface WebRTCRepository {
    fun observeConnectionEvents(peerId: String): Flow<WebRTCConnectionState> // 연결 상태 관찰
    fun observeReceivedData(peerId: String): Flow<ByteArray> // 데이터 수신 관찰
    suspend fun startConnection(fromPeerId:String,peerId: String): Result<Unit> // 연결 시작
    suspend fun sendData(peerId: String, data: ByteArray): Result<Unit> // 데이터 전송
    suspend fun sendBroadcastData(data: ByteArray): Result<Unit> // 전체 전송
    fun handleSignalingMessage(message: SignalingMessage) // 시그널링 메시지 처리 위임
    fun disconnect(peerId: String)
    fun disconnectAll()
    fun initializeWebRTC()
    fun observeAllReceivedData(): Flow<Pair<String, ByteArray>> // peerId, data
}