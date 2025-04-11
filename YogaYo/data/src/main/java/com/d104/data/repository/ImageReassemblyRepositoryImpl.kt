package com.d104.data.repository

import com.d104.data.remote.Service.ImageReassemblyService
import com.d104.domain.model.ImageChunkMessage
import com.d104.domain.model.MissingChunksInfo
import com.d104.domain.repository.ImageReassemblyRepository
import kotlinx.coroutines.flow.Flow
import okhttp3.internal.connection.retryTlsHandshake
import javax.inject.Inject

class ImageReassemblyRepositoryImpl @Inject constructor(
    private val imageReassemblyService: ImageReassemblyService
) : ImageReassemblyRepository {
    override fun processChunk(peerId:String,chunk: ImageChunkMessage) {
        imageReassemblyService.processChunk(peerId,chunk)
    }

    override fun observeImage(): Flow<ByteArray> {
        return imageReassemblyService.completedImages
    }

    override fun observeMissingChunks(): Flow<MissingChunksInfo> {
        return imageReassemblyService.missingChunksDetected
    }


}