package com.d104.domain.model

import kotlinx.coroutines.Job
import java.util.concurrent.ConcurrentHashMap

data class PeerImageBuffer(
    val peerId: String, // 이미지를 보낸 Peer의 ID
    val totalChunksExpected: Int,
    val receivedChunks: ConcurrentHashMap<Int, ByteArray> = ConcurrentHashMap(), // 청크 저장은 동시성 안전하게 유지
    var lastReceivedTimestamp: Long = System.currentTimeMillis(),
    var timeoutJob: Job? = null // 타임아웃 처리를 위한 Job
) {
    fun isComplete(): Boolean = receivedChunks.size == totalChunksExpected
}