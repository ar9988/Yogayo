package com.d104.yogaapp.utils

import android.content.Context


import android.graphics.*
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.SystemClock
import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.camera.core.ImageProxy
import com.d104.domain.model.Keypoint
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import javax.inject.Inject
import kotlin.math.min
import kotlin.math.pow
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.mediapipe.framework.image.ByteBufferExtractor
import java.util.concurrent.ConcurrentHashMap


/**
 * MediaPipe Pose Landmarker 작업을 처리하는 헬퍼 클래스.
 * Hilt를 통해 주입되며, ViewModel에서 사용됩니다.
 *
 * @param context 애플리케이션 컨텍스트.
 */
class PoseLandmarkerHelper @Inject constructor(
    @ApplicationContext val context: Context,
) {
    // 설정 가능한 옵션들
    var minPoseDetectionConfidence: Float = DEFAULT_POSE_DETECTION_CONFIDENCE
    var minPoseTrackingConfidence: Float = DEFAULT_POSE_TRACKING_CONFIDENCE
    var minPosePresenceConfidence: Float = DEFAULT_POSE_PRESENCE_CONFIDENCE
    var currentDelegate: Int = DELEGATE_CPU
    var runningMode: RunningMode = RunningMode.LIVE_STREAM
    var numPoses: Int = DEFAULT_NUM_POSES // 감지할 최대 포즈 수
    private val bitmapCache = ConcurrentHashMap<Long, Bitmap>()

    // 결과 및 에러를 전달할 리스너 (ViewModel에서 설정)
    var poseLandmarkerHelperListener: LandmarkerListener? = null

    private var poseLandmarker: PoseLandmarker? = null

    init {
        Log.d(TAG, "PoseLandmarkerHelper instance created.")
        // 초기화는 setupPoseLandmarker() 호출 시 진행
    }

    /**
     * 현재 설정을 사용하여 PoseLandmarker를 설정하거나 재설정합니다.
     * LIVE_STREAM 모드에서는 호출 전에 listener가 설정되어 있어야 합니다.
     */
    fun setupPoseLandmarker() {
        // 기존 인스턴스 정리
        clearPoseLandmarker()
        Log.d(TAG, "Setting up PoseLandmarker...")

        // LIVE_STREAM 모드에서 리스너 필수 체크
        if (runningMode == RunningMode.LIVE_STREAM && poseLandmarkerHelperListener == null) {
            val errorMsg = "Listener must be set before calling setupPoseLandmarker in LIVE_STREAM mode."
            Log.e(TAG, errorMsg)
            // 리스너가 null이어도 에러를 던지기보다 로그를 남기고, 실제 리스너 호출 시 null 체크
            // throw IllegalStateException(errorMsg) // -> 앱 크래시 대신 에러 로그 및 콜백 실패로 처리
            return // 초기화 중단
        }

        // 1. BaseOptions 설정
        val baseOptionBuilder = BaseOptions.builder()
        try {
            when (currentDelegate) {
                DELEGATE_CPU -> baseOptionBuilder.setDelegate(Delegate.CPU)
                DELEGATE_GPU -> baseOptionBuilder.setDelegate(Delegate.GPU)
                else -> baseOptionBuilder.setDelegate(Delegate.CPU) // 기본값 CPU
            }
            val modelName = "pose_landmarker_lite.task" // 모델 파일 이름 확인
            baseOptionBuilder.setModelAssetPath(modelName)
        } catch (e: Exception) {
            val errorMsg = "Failed to set BaseOptions: ${e.message}"
            Log.e(TAG, errorMsg, e)
            poseLandmarkerHelperListener?.onError(errorMsg)
            return // 초기화 중단
        }

        // 2. PoseLandmarkerOptions 설정
        try {
            val baseOptions = baseOptionBuilder.build()
            val optionsBuilder = PoseLandmarker.PoseLandmarkerOptions.builder()
                .setBaseOptions(baseOptions)
                .setRunningMode(runningMode)
                .setNumPoses(numPoses) // 감지할 포즈 수 설정
                .setMinPoseDetectionConfidence(minPoseDetectionConfidence)
                .setMinTrackingConfidence(minPoseTrackingConfidence)
                .setMinPosePresenceConfidence(minPosePresenceConfidence)

            // LIVE_STREAM 모드에서만 리스너 설정
            if (runningMode == RunningMode.LIVE_STREAM) {
                optionsBuilder
                    .setResultListener(this::returnLivestreamResult)
                    .setErrorListener(this::returnLivestreamError)
            }

            // 3. PoseLandmarker 생성
            val options = optionsBuilder.build()
            poseLandmarker = PoseLandmarker.createFromOptions(context, options)
            Log.d(TAG, "PoseLandmarker setup successful. RunningMode: $runningMode, Delegate: $currentDelegate")

        } catch (e: Exception) { // IllegalStateException, RuntimeException 등 포함
            val errorMsg = "Pose Landmarker failed to initialize: ${e.message}"
            Log.e(TAG, errorMsg, e)
            val errorCode = if (e.message?.contains("TfLiteGpuDelegate") == true) GPU_ERROR else OTHER_ERROR
            poseLandmarkerHelperListener?.onError(errorMsg, errorCode)
        }
    }

    /**
     * ImageProxy (YUV_420_888)를 Bitmap (ARGB_8888)으로 변환합니다.
     * 주의: 이 변환은 성능에 영향을 줄 수 있으며, 모든 기기에서 완벽하게 동작하지 않을 수 있습니다.
     *
     * @param imageProxy 변환할 ImageProxy 객체.
     * @return 변환된 Bitmap 또는 실패 시 null.
     */
    private fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap? {
        if (imageProxy.format != ImageFormat.YUV_420_888) {
            Log.e(TAG, "Unsupported image format: ${imageProxy.format}. Expected YUV_420_888.")
            return null
        }

        val yBuffer = imageProxy.planes[0].buffer
        val uBuffer = imageProxy.planes[1].buffer
        val vBuffer = imageProxy.planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        // NV21 포맷 데이터를 담을 바이트 배열 (Y 평면 크기 + U/V 평면 크기)
        // U/V는 Y에 비해 1/4 크기지만, NV21은 VUVU 순서로 YSize / 2 만큼의 공간 필요
        val nv21 = ByteArray(ySize + imageProxy.width * imageProxy.height / 2)
        // Y 평면 복사
        yBuffer.get(nv21, 0, ySize)

        // U/V 평면 처리 (NV21 형식: YYYY... VV UU... 또는 YYYY... VUVUVU...)
        val pixelStride = imageProxy.planes[1].pixelStride // U/V 픽셀 간격 (1: planar, 2: semi-planar/interleaved)
        val rowStride = imageProxy.planes[1].rowStride     // U/V 한 줄의 바이트 수

        // 임시 바이트 배열 사용 (DirectByteBuffer get(index) 성능 이슈 회피 및 안전성)
        val uBytes = ByteArray(uSize)
        uBuffer.get(uBytes)
        val vBytes = ByteArray(vSize)
        vBuffer.get(vBytes)

        var nv21Index = ySize // NV21 배열에서 U/V 데이터가 시작될 위치

        // U/V 데이터를 NV21 형식 (V 먼저, 그 다음 U, 인터리빙)으로 복사
        for (row in 0 until imageProxy.height / 2) {
            for (col in 0 until imageProxy.width / 2) {
                // 현재 U/V 픽셀 위치에 해당하는 인덱스 계산
                val uIndex: Int
                val vIndex: Int

                if (pixelStride == 1) { // Planar (UUUU... VVVV...)
                    // 평면 전체에서 현재 row, col에 맞는 인덱스 계산
                    uIndex = row * rowStride + col
                    vIndex = row * rowStride + col
                } else { // Semi-planar/Interleaved (UVUV... 또는 VUVU...)
                    // 일반적으로 plane[1]은 U/V 쌍, plane[2]는 V/U 쌍 또는 동일 데이터.
                    // pixelStride가 2이므로 한 쌍(U, V)씩 처리.
                    // plane[1] (U)와 plane[2] (V)의 row/col 위치 계산
                    val uvPixelIndex = col * pixelStride + row * rowStride
                    // 여기서는 plane[1] -> U, plane[2] -> V 가정 (정확한 포맷은 기기마다 다를 수 있음)
                    uIndex = uvPixelIndex
                    vIndex = uvPixelIndex // V 버퍼에서의 인덱스도 동일하다고 가정 (별도 버퍼 사용 시)
                    // 만약 plane[1]에 V, U가 섞여있다면 로직 변경 필요
                }

                // 인덱스 범위 체크 후 NV21 배열에 복사 (V 먼저, 그 다음 U)
                if (vIndex < vBytes.size && uIndex < uBytes.size && nv21Index + 1 < nv21.size) {
                    nv21[nv21Index++] = vBytes[vIndex] // V 데이터 복사
                    nv21[nv21Index++] = uBytes[uIndex] // U 데이터 복사
                } else {
                    // 인덱스 오류 가능성 로그 (너무 자주 발생하면 성능 저하)
                    if(nv21Index == ySize) { // 첫 에러만 로깅
                        Log.w(TAG, "Potential IndexOutOfBounds in NV21 conversion. " +
                                "vIndex=$vIndex(size=${vBytes.size}), uIndex=$uIndex(size=${uBytes.size}), nv21Index=$nv21Index(size=${nv21.size}), " +
                                "pixelStride=$pixelStride, rowStride=$rowStride, w=${imageProxy.width}, h=${imageProxy.height}")
                    }
                    // 부족한 부분은 0으로 채우거나, 처리를 중단해야 할 수 있음
                    // 여기서는 일단 다음 픽셀로 진행 (이미지가 깨질 수 있음)
                    if (nv21Index + 1 < nv21.size) { // 배열 범위 내면 0으로 채움
                        nv21[nv21Index++] = 0
                        nv21[nv21Index++] = 0
                    } else {
                        break // NV21 배열이 꽉 찼으면 루프 종료
                    }
                }
            }
            if (nv21Index >= nv21.size) break // 바깥 루프도 종료
        }

        // NV21 바이트 배열 -> YuvImage
        val yuvImage = try {
            YuvImage(nv21, ImageFormat.NV21, imageProxy.width, imageProxy.height, null)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create YuvImage: ${e.message}", e)
            return null
        }

        // YuvImage -> Jpeg Bytes -> Bitmap
        val out = ByteArrayOutputStream()
        return try {
            if (!yuvImage.compressToJpeg(Rect(0, 0, imageProxy.width, imageProxy.height), 95, out)) { // 품질 95
                Log.e(TAG, "Failed to compress YuvImage to Jpeg")
                return null
            }
            val imageBytes = out.toByteArray()
            BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to decode Jpeg to Bitmap: ${e.message}", e)
            null
        } finally {
            try { out.close() } catch (ioe: Exception) { /* ignore */ }
        }
    }


    /**
     * 실시간 스트림의 ImageProxy를 처리하여 포즈 감지를 수행합니다.
     * LIVE_STREAM 모드에서만 사용해야 합니다.
     *
     * @param imageProxy 분석할 이미지 프록시.
     * @param isFrontCamera 전면 카메라 사용 여부 (좌우 반전 적용 위함).
     */
    fun detectLiveStream(imageProxy: ImageProxy, isFrontCamera: Boolean) {
        if (runningMode != RunningMode.LIVE_STREAM) {
            Log.w(TAG,"Attempting detectLiveStream outside LIVE_STREAM mode.")
            imageProxy.close()
            return
        }
        if (poseLandmarker == null) {
            // Log.w(TAG, "PoseLandmarker not initialized yet.") // 너무 자주 로깅될 수 있음
            imageProxy.close()
            return
        }

        // 회전 정보 미리 읽기 (ImageProxy 닫기 전)
        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
        val frameTime = SystemClock.uptimeMillis()

        // ImageProxy -> Bitmap 변환
        val bitmap = imageProxyToBitmap(imageProxy)

        // 변환 후 ImageProxy 닫기
        try {
            imageProxy.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error closing imageProxy after conversion", e)
        }

        // Bitmap 변환 실패 시 종료
        if (bitmap == null) {
            Log.e(TAG, "Bitmap conversion failed. Cannot process frame.")
            // 필요시 에러 콜백 호출
            // poseLandmarkerHelperListener?.onError("Bitmap conversion failed")
            return
        }

        // Bitmap 회전 및 반전 적용
        val matrix = Matrix().apply {
            postRotate(rotationDegrees.toFloat())
            if (isFrontCamera) {
                postScale(-1f, 1f, bitmap.width / 2f, bitmap.height / 2f) // 중심 기준 반전
            }
        }

        val rotatedBitmap: Bitmap? = try {
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to rotate/flip bitmap: ${e.message}", e)
            null // 실패 시 null 반환
        } finally {
            // 원본 비트맵은 회전/반전 후 더 이상 필요 없으므로 즉시 해제
            bitmap.recycle()
        }

        // 회전된 비트맵 생성 실패 시 종료
        if (rotatedBitmap == null) {
            Log.e(TAG, "Rotated bitmap creation failed.")
            return
        }

        bitmapCache[frameTime] = rotatedBitmap

        // Bitmap -> MPImage 변환
        val mpImage = BitmapImageBuilder(rotatedBitmap).build()

        // 비동기 감지 실행
        detectAsync(mpImage, frameTime)

        // rotatedBitmap 메모리 관리 주의!
        // detectAsync는 비동기이며, MediaPipe가 언제 Bitmap 사용을 마칠지 알 수 없음.
        // 여기서 바로 recycle()하면 감지 중 크래시 발생 가능.
        // MPImage가 내부적으로 복사본을 만들지 않으면 메모리 누수 가능성 있음.
        // -> createFromImageProxy 사용 시 이런 문제 없음.
        // rotatedBitmap.recycle() // <<-- 여기서 호출하면 안됨!
    }

    /**
     * MPImage를 사용하여 비동기 포즈 감지를 실행합니다.
     * 결과는 설정된 리스너로 전달됩니다 (LIVE_STREAM 모드).
     *
     * @param mpImage 감지할 MPImage 객체.
     * @param frameTime 프레임 타임스탬프 (ms).
     */
    @VisibleForTesting
    fun detectAsync(mpImage: MPImage, frameTime: Long) {
        try {
            // detectAsync 호출 전 Landmarker null 체크 (이론상 detectLiveStream에서 걸러짐)
            poseLandmarker?.detectAsync(mpImage, frameTime)
                ?: Log.w(TAG, "detectAsync called but poseLandmarker is null")
        } catch (e: Exception) {
            Log.e(TAG, "Error calling poseLandmarker.detectAsync: ${e.message}", e)
            // 리스너를 통해 에러 전파 (LIVE_STREAM 모드)
            if (runningMode == RunningMode.LIVE_STREAM) {
                returnLivestreamError(RuntimeException("detectAsync failed: ${e.message}", e))
            }
        }
    }


    // --- 비디오 및 이미지 파일 처리 함수 (필요시 사용) ---

    fun detectVideoFile(videoUri: Uri, inferenceIntervalMs: Long): ResultBundle? {
        // ... (이전 제공 코드와 동일 - 검증 필요) ...
        // 내부적으로 Bitmap 변환, detectForVideo 호출
        return null // 임시
    }

    fun detectImage(image: Bitmap): ResultBundle? {
        // ... (이전 제공 코드와 동일 - 검증 필요) ...
        // detect 호출
        return null // 임시
    }

    // --- 리스너 콜백 함수 ---

    /**
     * LIVE_STREAM 모드에서 감지 결과를 리스너로 전달합니다.
     */
    private fun returnLivestreamResult(result: PoseLandmarkerResult, input: MPImage) {
        val finishTimeMs = SystemClock.uptimeMillis()
        val inferenceTime = finishTimeMs - result.timestampMs()
        Log.d(TAG,"Mediapipe inference inferenceTime=${inferenceTime}")

        val associatedBitmap = bitmapCache.remove(result.timestampMs())

        // 리스너가 null이 아닐 때만 호출
        if (associatedBitmap == null) {
            Log.w(TAG, "Result timestamp ${result.timestampMs()} 에 해당하는 Bitmap을 캐시에서 찾을 수 없습니다.")
            // 비트맵 없이 결과를 전달하거나 에러 처리
            poseLandmarkerHelperListener?.onResults(
                ResultBundle(
                    listOf(result),
                    inferenceTime,
                    input.height,
                    input.width,
                    image = null // Bitmap 없음
                )
            )
            return
        }
        poseLandmarkerHelperListener?.onResults(
            ResultBundle(
                listOf(result),
                inferenceTime,
                input.height,
                input.width,
                // 가져온 Bitmap의 복사본을 전달 (수정 방지 및 안전한 생명주기 관리)
                image = associatedBitmap.copy(associatedBitmap.config, true)
            )
        )
    }

    /**
     * LIVE_STREAM 모드에서 발생한 에러를 리스너로 전달합니다.
     */
    private fun returnLivestreamError(error: RuntimeException) {
        // 리스너가 null이 아닐 때만 호출
        poseLandmarkerHelperListener?.onError(
            error.message ?: "An unknown error has occurred during livestream",
            OTHER_ERROR // 필요시 에러 코드 구분 로직 추가
        )
        Log.e(TAG, "Livestream error reported: ${error.message}", error)
    }

    /**
     * PoseLandmarker 리소스를 해제합니다.
     */
    fun clearPoseLandmarker() {
        poseLandmarker?.close()
        poseLandmarker = null
        Log.d(TAG, "PoseLandmarker closed.")
    }

    /**
     * PoseLandmarker가 현재 닫혀있는지(초기화되지 않았거나 해제되었는지) 확인합니다.
     */
    fun isClose(): Boolean {
        return poseLandmarker == null
    }



    fun clear() {
        bitmapCache.values.forEach { it.recycle() } // 캐시된 모든 비트맵 해제
        bitmapCache.clear()
        // poseLandmarker?.close() 등 다른 리소스 정리
    }


    companion object {
        const val TAG = "PoseLandmarkerHelper"

        // Delegate 선택 상수
        const val DELEGATE_CPU = 0
        const val DELEGATE_GPU = 1

        // 기본 Confidence 값
        const val DEFAULT_POSE_DETECTION_CONFIDENCE = 0.5F
        const val DEFAULT_POSE_TRACKING_CONFIDENCE = 0.5F
        const val DEFAULT_POSE_PRESENCE_CONFIDENCE = 0.5F

        // 기본 감지 포즈 수
        const val DEFAULT_NUM_POSES = 1

        // 에러 코드 상수
        const val OTHER_ERROR = 0
        const val GPU_ERROR = 1 // GPU 관련 에러 식별용
    }

    /**
     * 감지 결과와 관련 메타데이터를 담는 데이터 클래스.
     *
     * @param results 감지된 포즈 결과 목록 (보통 1개).
     * @param inferenceTime 추론에 걸린 시간 (ms).
     * @param inputImageHeight 입력 이미지 높이.
     * @param inputImageWidth 입력 이미지 너비.
     */
    data class ResultBundle(
        val results: List<PoseLandmarkerResult>,
        val inferenceTime: Long,
        val inputImageHeight: Int,
        val inputImageWidth: Int,
        val image:Bitmap?
    )

    /**
     * PoseLandmarkerHelper의 결과 및 에러를 수신하는 리스너 인터페이스.
     * ViewModel에서 구현합니다.
     */
    interface LandmarkerListener {
        /**
         * 에러 발생 시 호출됩니다.
         * @param error 에러 메시지.
         * @param errorCode 에러 코드 (기본값 OTHER_ERROR).
         */
        fun onError(error: String, errorCode: Int = OTHER_ERROR)

        /**
         * 감지 결과 수신 시 호출됩니다.
         * @param resultBundle 감지 결과 데이터 번들.
         */
        fun onResults(resultBundle: ResultBundle)
    }
}