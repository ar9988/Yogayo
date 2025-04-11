package com.d104.domain.model

data class PeerImageBuffer(
    val totalChunksExpected: Int,
    val receivedChunks: MutableMap<Int, ByteArray> = mutableMapOf(), // Key: chunkIndex, Value: 디코딩된 ByteArray
    var lastReceivedTimestamp: Long = System.currentTimeMillis() // 타임아웃 처리를 위한 타임스탬프 (선택적)
) {
    fun isComplete(): Boolean = receivedChunks.size == totalChunksExpected
}