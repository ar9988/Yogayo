package com.d104.data.remote.Service

import android.util.Base64 // 안드로이드 Base64 사용
import android.util.Log
import com.d104.domain.model.ImageChunkMessage
import com.d104.domain.model.PeerImageBuffer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.concurrent.ConcurrentHashMap // 동시성 처리를 위해 ConcurrentHashMap 사용
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageReassemblyService @Inject constructor() {

    private val TAG = "ImageReassemblySvc"
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // 현재 재조립 중인 단일 이미지 버퍼
    private var currentImageBuffer: PeerImageBuffer? = null

    // 재조립 완료된 이미지를 외부로 알리기 위한 Flow (이제 ByteArray 만 전달)
    private val _completedImages = MutableSharedFlow<ByteArray>(
        extraBufferCapacity = 10, // 버퍼 설정 (필요에 따라 조절)
        onBufferOverflow = BufferOverflow.DROP_OLDEST // 버퍼 오버플로우 시 오래된 것 삭제
    )
    val completedImages: SharedFlow<ByteArray> = _completedImages.asSharedFlow()

    /**
     * 수신된 이미지 청크를 처리합니다.
     * 새로운 이미지의 첫 청크가 오면 이전 버퍼는 초기화됩니다.
     * 모든 청크가 도착하면 이미지를 재조립하고 completedImages Flow 로 방출합니다.
     * @param chunk 수신된 ImageChunkMessage 객체
     */
    fun processChunk(chunk: ImageChunkMessage): Job {
        return serviceScope.launch { // 각 청크 처리는 별도 코루틴에서
            Log.v(TAG, "Processing chunk ${chunk.chunkIndex + 1}/${chunk.totalChunks}")

            // 1. Base64 데이터 디코딩
            val decodedData: ByteArray? = try {
                Base64.decode(chunk.dataBase64, Base64.DEFAULT)
            } catch (e: IllegalArgumentException) {
                Log.e(TAG, "Failed to decode Base64 data for chunk ${chunk.chunkIndex}", e)
                null
            }

            if (decodedData == null) {
                return@launch // 디코딩 실패 시 처리 중단
            }

            // 2. 버퍼 관리: 새 이미지 시작 여부 확인
            // chunkIndex가 0이거나, 현재 버퍼가 없거나, 예상 청크 수가 다르면 새 버퍼 시작
            if (chunk.chunkIndex == 0 || currentImageBuffer == null || currentImageBuffer?.totalChunksExpected != chunk.totalChunks) {
                if (chunk.chunkIndex == 0) {
                    Log.d(TAG, "Starting new image assembly (expecting ${chunk.totalChunks} chunks)")
                } else {
                    // 첫 청크가 아닌데 버퍼가 없거나 totalChunks가 다른 경우 (오류 상황일 수 있음)
                    Log.w(TAG, "Inconsistent chunk received (index ${chunk.chunkIndex} but no valid buffer or mismatched total). Starting new buffer anyway.")
                }
                currentImageBuffer = PeerImageBuffer(totalChunksExpected = chunk.totalChunks)
            }

            // 3. 현재 버퍼에 디코딩된 청크 추가
            // currentImageBuffer 는 여기서 null 이 아님이 보장되어야 함 (위 로직 점검)
            currentImageBuffer?.let { buffer ->
                buffer.receivedChunks[chunk.chunkIndex] = decodedData // putIfAbsent 대신 put 사용 (재전송된 청크 덮어쓰기 가능)
                buffer.lastReceivedTimestamp = System.currentTimeMillis() // 타임스탬프 업데이트

                // 4. 모든 청크가 도착했는지 확인
                if (buffer.isComplete()) {
                    Log.i(TAG, "All chunks received for the image. Reassembling...")
                    val bufferToReassemble = buffer // 재조립할 버퍼 저장
                    currentImageBuffer = null // 현재 버퍼 비우기 (다음 이미지 준비)
                    reassembleAndEmitImage(bufferToReassemble) // 재조립 시작
                } else {
                    Log.v(TAG, "Received ${buffer.receivedChunks.size}/${buffer.totalChunksExpected} chunks.")
                }
            } ?: run {
                Log.e(TAG, "currentImageBuffer is unexpectedly null after check/creation logic.")
            }
        }
    }

    /**
     * 완성된 버퍼로부터 이미지를 재조립하여 Flow로 방출합니다. (peerId 제거)
     */
    private suspend fun reassembleAndEmitImage(buffer: PeerImageBuffer) {
        Log.d(TAG, "reassembleAndEmitImage started for buffer with ${buffer.receivedChunks.size}/${buffer.totalChunksExpected} chunks.")
        withContext(Dispatchers.IO) {
            try {
                val outputStream = ByteArrayOutputStream()
                var success = true
                for (i in 0 until buffer.totalChunksExpected) {
                    val chunkBytes = buffer.receivedChunks[i]
                    if (chunkBytes != null) {
                        outputStream.write(chunkBytes)
                    } else {
                        Log.e(TAG, "Missing chunk index $i while reassembling image.")
                        success = false
                        break
                    }
                }

                if (success) {
                    val completeImageData = outputStream.toByteArray()
                    Log.d(TAG, "Image reassembled successfully. Size: ${completeImageData.size}. Attempting to emit.") // <<<--- 로그 추가
                    _completedImages.emit(completeImageData) // 완성된 ByteArray 방출
                    Log.d(TAG, "Successfully emitted completed image ByteArray.") // <<<--- 로그 추가

                } else {
                    Log.w(TAG, "Image reassembly skipped due to missing chunks.")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during image reassembly: ${e.message}", e)
            }
        }
    }

    /**
     * 현재 진행 중인 이미지 버퍼를 강제로 정리합니다.
     */
    fun clearCurrentBuffer() {
        Log.d(TAG, "Clearing current image buffer.")
        currentImageBuffer = null
    }

    /**
     * 모든 이미지 버퍼를 정리합니다. (이 클래스에서는 현재 버퍼만 정리)
     */
    fun clearAllBuffers() { // 이름은 유지하되, 동작은 현재 버퍼 정리
        clearCurrentBuffer()
    }
}