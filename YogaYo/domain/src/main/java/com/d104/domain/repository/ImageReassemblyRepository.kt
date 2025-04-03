package com.d104.domain.repository

import com.d104.domain.model.ImageChunkMessage
import kotlinx.coroutines.flow.Flow

interface ImageReassemblyRepository{
    fun processChunk(chunk:ImageChunkMessage)
    fun observeImage(): Flow<ByteArray>
}