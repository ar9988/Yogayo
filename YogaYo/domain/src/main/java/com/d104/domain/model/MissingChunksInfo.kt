package com.d104.domain.model

data class MissingChunksInfo(
    val peerId: String,
    val missingIndices: List<Int>,
    val totalChunks: Int
)