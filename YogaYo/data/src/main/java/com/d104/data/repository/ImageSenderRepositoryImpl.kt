package com.d104.data.repository

import android.util.Log
import com.d104.data.remote.Service.ImageSenderService
import com.d104.domain.repository.ImageSenderRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class ImageSenderRepositoryImpl @Inject constructor(
    private val imageSenderService: ImageSenderService,
    // 필요하다면 CoroutineScope 주입
    private val appScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) : ImageSenderRepository {

    private val TAG = "ImageSenderRepoImpl"

    override suspend fun sendImageToPeer(peerId: String, imageBytes: ByteArray, quality: Int): Result<Unit> {
        return try {
            Log.d(TAG, "Requesting send image to peer $peerId (quality: $quality)")
            // ImageSenderService의 sendImage는 Job을 반환하므로,
            // UseCase가 즉시 결과를 반환하게 하려면 launch만 호출하거나,
            // Job의 완료를 기다리려면 join()을 사용 (여기서는 launch만 호출)
            // 에러 처리는 invokeOnCompletion 등을 사용 가능
            val job = imageSenderService.sendImage(
                imageByteArray = imageBytes,
                targetPeerId = peerId,
                quality = quality
            )
            // Job의 예외 처리를 위해 invokeOnCompletion 사용 (선택적)
            job.invokeOnCompletion { throwable ->
                if (throwable != null) {
                    Log.e(TAG, "Error occurred during image sending job to $peerId", throwable)
                    // TODO: 에러 상태를 외부에 알리는 로직 (예: Flow 사용)
                }
            }
            Result.success(Unit) // Job 실행 요청 성공
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initiate send image to peer $peerId", e)
            Result.failure(e)
        }
    }

    override suspend fun broadcastImage(imageBytes: ByteArray, quality: Int): Result<Unit> {
        return try {
            Log.d(TAG, "Requesting broadcast image (quality: $quality)")
            val job = imageSenderService.sendImage(
                imageByteArray = imageBytes,
                targetPeerId = null, // null 로 브로드캐스트
                quality = quality
            )
            job.invokeOnCompletion { throwable ->
                if (throwable != null) {
                    Log.e(TAG, "Error occurred during image broadcasting job", throwable)
                    // TODO: 에러 상태 알림
                }
            }
            Result.success(Unit) // Job 실행 요청 성공
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initiate broadcast image", e)
            Result.failure(e)
        }
    }
}