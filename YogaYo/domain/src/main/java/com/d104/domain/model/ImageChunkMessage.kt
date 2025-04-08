package com.d104.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("image_chunk") // 큰 이미지를 위한 청크 예시
data class ImageChunkMessage(
    val chunkIndex: Int,
    val totalChunks: Int,
    val dataBase64: String, // Base64 인코딩된 이미지 청크 데이터
) : DataChannelMessage()

