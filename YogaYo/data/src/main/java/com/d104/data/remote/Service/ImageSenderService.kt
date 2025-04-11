package com.d104.data.remote.Service


import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import com.d104.domain.model.DataChannelMessage
import com.d104.domain.model.ImageChunkMessage
import com.d104.domain.repository.WebRTCRepository // WebRTC 데이터 전송 인터페이스
import kotlinx.coroutines.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min

@Singleton
class ImageSenderService @Inject constructor(
    private val webRTCRepository: WebRTCRepository, // 데이터 전송을 위해 주입
    private val json: Json // JSON 직렬화를 위해 주입
) {
    private val TAG = "ImageSenderService"
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // 청크 크기 (바이트 단위) - 네트워크 환경 및 MTU 고려하여 조절 필요
    // WebRTC DataChannel 은 보통 꽤 큰 메시지도 잘 처리하지만, 안정성을 위해 분할
    // 너무 작으면 오버헤드가 크고, 너무 크면 전송 실패/지연 가능성
    private val CHUNK_SIZE = 16 * 1024 // 예시: 16KB

    /**
     * 이미지를 청크로 나누어 지정된 피어 또는 모든 피어에게 전송합니다.
     * @param imageByteArray 전송할 이미지의 원본 ByteArray
     * @param targetPeerId 특정 피어에게 보낼 경우 해당 peerId, null 이면 브로드캐스트
     * @param quality JPEG 압축 품질 (0-100, 기본값 85)
     */
    fun sendImage(
        imageByteArray: ByteArray,
        targetPeerId: String? = null, // null 이면 broadcast
        quality: Int = 85 // 이미지 압축 품질 추가
    ): Job {
        return serviceScope.launch {
            Log.d(TAG, "Starting to send image (${imageByteArray.size} bytes) to ${targetPeerId ?: "all peers"}")

            // 1. 이미지 압축 (선택적이지만 매우 권장) - IO 디스패처 사용
            val compressedBytes = try {
                withContext(Dispatchers.IO) {
                    compressImage(imageByteArray, quality)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to compress image", e)
                imageByteArray // 압축 실패 시 원본 사용 (또는 에러 처리)
            }
            Log.d(TAG, "Image compressed size: ${compressedBytes.size} bytes (Quality: $quality)")

            // 2. 이미지 데이터를 청크로 분할
            val base64String = Base64.encodeToString(compressedBytes, Base64.NO_WRAP)
            val totalChunks = (base64String.length + CHUNK_SIZE - 1) / CHUNK_SIZE // Base64 문자열 길이 기준
            Log.d(TAG, "Total chunks to send (from Base64 string): $totalChunks")

// 3. Base64 문자열을 청크로 나누어 전송
            for (i in 0 until totalChunks) {
                val start = i * CHUNK_SIZE
                val end = min(start + CHUNK_SIZE, base64String.length)
                val chunkData = base64String.substring(start, end) // Base64 문자열 조각
                val chunkMessage = ImageChunkMessage(
                    chunkIndex = i,
                    totalChunks = totalChunks,
                    dataBase64 = chunkData // Base64 문자열 청크 전달
                )

                try {
                    val messageJson = json.encodeToString(DataChannelMessage.serializer(), chunkMessage)
                    Log.v(TAG, "Sending chunk ${i + 1}/$totalChunks (${chunkData.length} chars)") // 로그 수정
                    webRTCRepository.sendBroadcastData(messageJson.toByteArray()) // JSON 문자열을 ByteArray로 변환하여 전송
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to encode or send chunk $i", e)
                    break // 에러 시 중단
                }
                // delay(1) // 딜레이 고려
            }
            Log.i(TAG, "Finished sending all $totalChunks chunks for the image.")
        }
    }

    /**
     * 이미지를 JPEG 형식으로 압축합니다. (다른 형식 원하면 수정)
     */
    private suspend fun compressImage(originalBytes: ByteArray, quality: Int): ByteArray {
        return withContext(Dispatchers.IO) { // 이미지 처리는 IO 스레드에서
            var bitmap: Bitmap? = null
            var outputStream: ByteArrayOutputStream? = null
            try {
                // 비트맵으로 디코딩
                bitmap = BitmapFactory.decodeByteArray(originalBytes, 0, originalBytes.size)
                if (bitmap == null) throw IllegalArgumentException("Could not decode image byte array")

                // JPEG 으로 압축
                outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream) // JPEG 사용, quality 적용
                outputStream.toByteArray() // 압축된 바이트 배열 반환
            } finally {
                bitmap?.recycle() // 비트맵 메모리 해제
                try {
                    outputStream?.close()
                } catch (e: Exception) { /* ignore */ }
            }
        }
    }
}