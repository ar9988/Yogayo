package com.d104.data.remote.Service

import android.util.Base64 // 안드로이드 Base64 사용
import android.util.Log
import com.d104.domain.model.ImageChunkMessage
import com.d104.domain.model.MissingChunksInfo
import com.d104.domain.model.PeerImageBuffer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
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
    @Volatile // 가시성 확보 (선택적, synchronized로 충분할 수 있음)
    private var currentImageBuffer: PeerImageBuffer? = null
    private val bufferLock = Any() // synchronized 블록용 잠금 객체

    // 재조립 완료된 이미지를 외부로 알리기 위한 Flow
    private val _completedImages = MutableSharedFlow<ByteArray>(
        replay = 1,
        extraBufferCapacity = 10,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val completedImages: SharedFlow<ByteArray> = _completedImages.asSharedFlow()

    // 누락된 청크 정보를 외부로 알리기 위한 Flow
    private val _missingChunksDetected = MutableSharedFlow<MissingChunksInfo>(
        extraBufferCapacity = 5,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val missingChunksDetected: SharedFlow<MissingChunksInfo> = _missingChunksDetected.asSharedFlow()

    // 청크 수신 타임아웃 시간 (예: 10초)
    private val CHUNK_RECEIVAL_TIMEOUT_MS = 2_000L

    /**
     * 수신된 이미지 청크를 처리합니다.
     * @param peerId 청크를 보낸 Peer의 ID
     * @param chunk 수신된 ImageChunkMessage 객체
     */
    fun processChunk(peerId: String, chunk: ImageChunkMessage): Job {
        return serviceScope.launch {
            Log.v(TAG, "[Peer $peerId] Processing chunk ${chunk.chunkIndex + 1}/${chunk.totalChunks}")

            // 1. Base64 데이터 디코딩 (기존과 동일)
            val decodedData: ByteArray? = try {
                Base64.decode(chunk.dataBase64, Base64.DEFAULT)
            } catch (e: IllegalArgumentException) {
                Log.e(TAG, "[Peer $peerId] Failed to decode Base64 for chunk ${chunk.chunkIndex}", e)
                null
            }

            if (decodedData == null) {
                return@launch
            }

            // 2. 버퍼 관리 및 타임아웃 설정 (synchronized 사용)
            var bufferToProcess: PeerImageBuffer? = null
            var startNewTimeout = false // 새 타임아웃 시작 여부 플래그

            synchronized(bufferLock) { // currentImageBuffer 접근 동기화
                val existingBuffer = currentImageBuffer

                // --- 수정된 로직 시작 ---
                if (chunk.chunkIndex == 0) {
                    // 청크 0은 항상 새로운 이미지 시작으로 간주
                    existingBuffer?.timeoutJob?.cancel("New image started with chunk 0")
                    Log.i(TAG, "[Peer $peerId] Starting new image assembly (expecting ${chunk.totalChunks} chunks). Timeout started.")
                    currentImageBuffer = PeerImageBuffer(peerId = peerId, totalChunksExpected = chunk.totalChunks)
                    bufferToProcess = currentImageBuffer // 새로 만든 버퍼를 처리 대상으로 설정
                    startNewTimeout = true // 새 타임아웃 시작 플래그 설정

                } else {
                    // 청크 0이 아닐 때: 현재 버퍼가 존재하고, peerId와 totalChunks가 일치하는 경우에만 사용
                    if (existingBuffer != null && existingBuffer.peerId == peerId && existingBuffer.totalChunksExpected == chunk.totalChunks) {
                        bufferToProcess = existingBuffer // 기존 버퍼 사용
                    } else {
                        // 현재 진행 중인 버퍼가 없거나, 다른 이미지의 청크가 도착한 경우
                        // 이 청크는 무시 -> 새 타임아웃을 시작하지 않음!
                        bufferToProcess = null
                        Log.w(TAG, "[Peer $peerId] Received chunk ${chunk.chunkIndex}/${chunk.totalChunks} but no matching active buffer found (Index != 0). Ignoring chunk.")
                    }
                }
                // --- 수정된 로직 끝 ---
            } // synchronized 끝

            // 새 타임아웃 시작 (synchronized 블록 외부에서 호출)
            if (startNewTimeout && bufferToProcess != null) {
                // bufferToProcess는 여기서 null이 아님 (chunkIndex == 0 이므로)
                bufferToProcess!!.timeoutJob = startTimeoutChecker(bufferToProcess!!)
            }

            // 3. 현재 버퍼에 청크 추가 (처리 대상 버퍼가 있는 경우)
            if (bufferToProcess != null) {
                bufferToProcess!!.receivedChunks[chunk.chunkIndex] = decodedData
                bufferToProcess!!.lastReceivedTimestamp = System.currentTimeMillis()

                // 4. 모든 청크 도착 확인 (기존과 동일)
                if (bufferToProcess!!.isComplete()) {
                    var bufferToReassemble: PeerImageBuffer? = null
                    synchronized(bufferLock) {
                        if (currentImageBuffer == bufferToProcess) {
                            Log.i(TAG, "[Peer $peerId] All chunks received. Clearing buffer and preparing for reassembly...")
                            currentImageBuffer?.timeoutJob?.cancel("Image completed")
                            bufferToReassemble = currentImageBuffer
                            currentImageBuffer = null
                        } else {
                            Log.w(TAG,"[Peer $peerId] Buffer was replaced before completion check finished for the completed buffer.")
                        }
                    }
                    bufferToReassemble?.let { reassembleAndEmitImage(it) }
                } else {
                    Log.v(TAG, "[Peer $peerId] Received ${bufferToProcess!!.receivedChunks.size}/${bufferToProcess!!.totalChunksExpected} chunks.")
                }
            }
            // bufferToProcess가 null이면 (무시된 청크) 아무 작업도 하지 않음
        }
    }

    /**
     * 지정된 버퍼에 대한 타임아웃 검사 코루틴을 시작합니다.
     */
    private fun startTimeoutChecker(buffer: PeerImageBuffer): Job {
        return serviceScope.launch {
            delay(CHUNK_RECEIVAL_TIMEOUT_MS)

            // 타임아웃 시점에도 이 버퍼가 여전히 현재 버퍼이고, 완료되지 않았는지 확인 (synchronized)
            val shouldCheckMissing = synchronized(bufferLock) {
                isActive && currentImageBuffer == buffer && !buffer.isComplete()
            }

            if (shouldCheckMissing) {
                Log.w(TAG, "[Peer ${buffer.peerId}] Timeout reached (${CHUNK_RECEIVAL_TIMEOUT_MS}ms). Checking for missing chunks.")
                checkAndEmitMissingChunks(buffer)
            } else if (isActive) {
                Log.d(TAG, "[Peer ${buffer.peerId}] Timeout checker finished: Image already completed or buffer replaced.")
            } else {
                Log.d(TAG, "[Peer ${buffer.peerId}] Timeout checker cancelled.")
            }
        }
    }

    /**
     * 버퍼에서 누락된 청크를 확인하고 _missingChunksDetected Flow로 방출합니다.
     */
    private suspend fun checkAndEmitMissingChunks(buffer: PeerImageBuffer) {
        // 이 함수 호출 시점에는 buffer가 currentImageBuffer와 동일하다고 가정됨 (startTimeoutChecker에서 확인)
        // 하지만 상태 변경 가능성을 고려하여 한번 더 확인하거나, 또는 이 함수 자체를 synchronized 블록에서 호출 고려

        val missingIndices = mutableListOf<Int>()
        // ConcurrentHashMap은 순회를 보장하지만, 안전하게 하려면 키셋을 복사하거나 동기화 고려
        val expectedIndices = (0 until buffer.totalChunksExpected).toSet()
        val receivedIndices = buffer.receivedChunks.keys // 현재 받은 인덱스 Set
        missingIndices.addAll(expectedIndices - receivedIndices) // 차집합으로 누락된 것 찾기

        if (missingIndices.isNotEmpty()) {
            Log.w(TAG, "[Peer ${buffer.peerId}] Missing chunks detected: ${missingIndices.joinToString()}. Emitting request signal.")
            val missingInfo = MissingChunksInfo(
                peerId = buffer.peerId,
                missingIndices = missingIndices,
                totalChunks = buffer.totalChunksExpected
            )
            _missingChunksDetected.emit(missingInfo) // 재요청 시그널 방출

            // --- 버퍼 제거 로직 삭제 ---
            // synchronized(bufferLock) {
            //      if (currentImageBuffer == buffer) {
            //          Log.d(TAG, "[Peer ${buffer.peerId}] Clearing buffer after timeout and emitting missing chunk info.") // 이 로그도 수정 필요
            //          currentImageBuffer = null
            //      }
            // }
            // --- 삭제 끝 ---

            // 타임아웃 Job은 이미 완료되었으므로 여기서 별도로 취소할 필요 없음
            Log.d(TAG, "[Peer ${buffer.peerId}] Timeout occurred and missing chunk signal emitted. Buffer remains active.")


        } else {
            // 이론상 타임아웃인데 누락 청크가 없는 경우 (매우 드물거나 이미 완료됨)
            // 이 경우에도 버퍼를 제거할 필요는 없음 (완료 시 다른 경로로 제거됨)
            Log.i(TAG, "[Peer ${buffer.peerId}] Timeout occurred, but no missing chunks found (possibly completed during check). Buffer remains active.")
            // synchronized(bufferLock) {
            //      if (currentImageBuffer == buffer) {
            //          currentImageBuffer = null // 이 로직도 필요 없음
            //      }
            // }
        }
    }

    /**
     * 완성된 버퍼로부터 이미지를 재조립하여 Flow로 방출합니다.
     */
    private suspend fun reassembleAndEmitImage(buffer: PeerImageBuffer) {
        Log.d(TAG, "[Peer ${buffer.peerId}] reassembleAndEmitImage started.")
        withContext(Dispatchers.IO) {
            try {
                val outputStream = ByteArrayOutputStream()
                var success = true
                for (i in 0 until buffer.totalChunksExpected) {
                    val chunkBytes = buffer.receivedChunks[i]
                    if (chunkBytes != null) {
                        outputStream.write(chunkBytes)
                    } else {
                        Log.e(TAG, "[Peer ${buffer.peerId}] CRITICAL: Missing chunk index $i during final reassembly!")
                        success = false
                        break
                    }
                }

                if (success) {
                    val completeImageData = outputStream.toByteArray()
                    Log.i(TAG, "[Peer ${buffer.peerId}] Image reassembled successfully. Size: ${completeImageData.size}. Emitting...")
                    _completedImages.emit(completeImageData)
                    Log.d(TAG, "[Peer ${buffer.peerId}] Successfully emitted completed image ByteArray.")

                } else {
                    Log.w(TAG, "[Peer ${buffer.peerId}] Image reassembly failed due to missing chunks.")
                }
            } catch (e: Exception) {
                Log.e(TAG, "[Peer ${buffer.peerId}] Error during image reassembly: ${e.message}", e)
            }
        }
    }

    /**
     * 현재 진행 중인 이미지 버퍼를 강제로 정리합니다.
     */
    fun clearCurrentBuffer() { // suspend 제거 가능 (synchronized 블록은 suspend 아님)
        synchronized(bufferLock) {
            if (currentImageBuffer != null) {
                Log.d(TAG, "[Peer ${currentImageBuffer?.peerId}] Clearing current image buffer manually.")
                currentImageBuffer?.timeoutJob?.cancel("Buffer cleared manually")
                currentImageBuffer = null
            }
        }
    }

    /**
     * 모든 이미지 버퍼를 정리합니다. (단일 버퍼 관리이므로 clearCurrentBuffer와 동일)
     */
    fun clearAllBuffers() { // suspend 제거 가능
        clearCurrentBuffer()
    }
}