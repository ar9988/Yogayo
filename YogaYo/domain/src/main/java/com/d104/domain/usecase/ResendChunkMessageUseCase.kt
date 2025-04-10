package com.d104.domain.usecase

import com.d104.domain.model.ChunkReRequest
import com.d104.domain.model.DataChannelMessage
import com.d104.domain.model.ImageChunkMessage
import com.d104.domain.repository.ImageSenderRepository
import com.d104.domain.repository.WebRTCRepository
import com.d104.domain.utils.Base64Encoder
import com.d104.domain.utils.ImageCompressor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import kotlin.io.encoding.Base64
import kotlin.math.min

class ResendChunkMessageUseCase @Inject constructor(
    private val webRTCRepository: WebRTCRepository,
    private val json: Json,
    private val base64Encoder: Base64Encoder, // Base64 인코더 인터페이스 주입
    private val imageCompressor: ImageCompressor // 이미지 압축 인터페이스 주입
) {
    private val CHUNK_SIZE = 16 * 1024

    // compressImage 함수 제거됨 -> imageCompressor 인터페이스 사용

    suspend operator fun invoke(
        targetPeerId: String,
        originalImageBytes: ByteArray,
        chunkReRequest: ChunkReRequest,
        quality: Int = 85
    ) {
        val missingIndices = chunkReRequest.missingIndices
        val totalChunksHint = chunkReRequest.totalChunks

        if (missingIndices.isEmpty()) {
            println("Resend request for $targetPeerId ignored: missingIndices list is empty.")
            return
        }
        println("Resending chunks for $targetPeerId. Quality: $quality, Missing: ${missingIndices.joinToString()}")

        try {
            // 1. 이미지 압축 (인터페이스 사용) - withContext 필요 시 Compressor 내부에서 처리하거나 여기서 감싸기
            val compressedBytes = withContext(Dispatchers.IO) { // 압축은 IO 작업일 수 있음
                imageCompressor.compress(originalImageBytes, quality) // JPEG 기본 가정
            }
            println("Re-compressed image size for resend: ${compressedBytes.size} bytes")

            // 2. Base64 변환 (인터페이스 사용)
            val base64String = base64Encoder.encodeToString(compressedBytes)

            // 3. 총 청크 수 계산
            val actualTotalChunks = (base64String.length + CHUNK_SIZE - 1) / CHUNK_SIZE
            if (actualTotalChunks != totalChunksHint) {
                println("Total chunks mismatch for resend from $targetPeerId. Hint: $totalChunksHint, Actual: $actualTotalChunks. Using actual count.")
            }

            // 4. 누락 청크 전송
            for (index in missingIndices) {
                if (index < 0 || index >= actualTotalChunks) {
                    println("Invalid missing index $index requested by $targetPeerId (Actual total: $actualTotalChunks). Skipping.")
                    continue
                }

                val start = index * CHUNK_SIZE
                val end = min(start + CHUNK_SIZE, base64String.length)
                val chunkData = base64String.substring(start, end)

                val chunkMessage = ImageChunkMessage(
                    chunkIndex = index,
                    totalChunks = actualTotalChunks,
                    dataBase64 = chunkData
                )
                val messageJson = json.encodeToString(DataChannelMessage.serializer(), chunkMessage)

                try {
                    webRTCRepository.sendData(targetPeerId, messageJson.toByteArray())
                    println("Resent chunk $index/$actualTotalChunks to $targetPeerId")
                    // kotlinx.coroutines.delay(10) // Optional delay
                } catch (sendError: Exception) {
                    println("Failed to resend chunk $index to $targetPeerId")
                }
            }
            println("Finished resending chunks to $targetPeerId.")

        } catch (e: Exception) {
            println("Error during chunk resend process for $targetPeerId")
        }
    }
}