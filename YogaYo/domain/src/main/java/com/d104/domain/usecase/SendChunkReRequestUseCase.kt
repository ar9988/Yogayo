package com.d104.domain.usecase

import com.d104.domain.model.ChunkReRequest
import com.d104.domain.model.MissingChunksInfo
import com.d104.domain.repository.WebRTCRepository
import com.d104.domain.utils.toByteArray
import kotlinx.serialization.json.Json
import javax.inject.Inject

class SendChunkReRequestUseCase @Inject constructor(
    private val webRTCRepository: WebRTCRepository,
    private val json: Json
) {
    suspend operator fun invoke(myId:String,missingChunksInfo: MissingChunksInfo) {
        webRTCRepository.sendData(missingChunksInfo.peerId, ChunkReRequest(
            fromPeerId = myId,
            missingChunksInfo.missingIndices,
            missingChunksInfo.totalChunks
        ).toByteArray(json))
    }
}