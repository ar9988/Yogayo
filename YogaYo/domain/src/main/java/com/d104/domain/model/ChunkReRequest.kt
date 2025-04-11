package com.d104.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("request_chunk") // 큰 이미지를 위한 청크 예시
data class ChunkReRequest(
    val fromPeerId: String,
    val missingIndices: List<Int>,
    val totalChunks: Int
) : DataChannelMessage()