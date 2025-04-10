package com.d104.domain.repository

import com.d104.domain.model.ImageChunkMessage
import com.d104.domain.model.MissingChunksInfo
import kotlinx.coroutines.flow.Flow

interface ImageReassemblyRepository{
    fun processChunk(peerId:String,chunk:ImageChunkMessage)
    fun observeImage(): Flow<ByteArray>
    fun observeMissingChunks(): Flow<MissingChunksInfo>
}