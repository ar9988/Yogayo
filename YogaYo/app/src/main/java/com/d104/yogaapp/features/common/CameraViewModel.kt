package com.d104.yogaapp.features.common

import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d104.domain.model.Keypoint
import com.d104.domain.model.YogaPose
import com.d104.yogaapp.utils.BestPoseModelUtil
import com.d104.yogaapp.utils.PoseLandmarkerHelper
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject
import kotlin.math.pow

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val poseLandmarkerHelper: PoseLandmarkerHelper,
    private val bestPoseModelUtil: BestPoseModelUtil
) : ViewModel(), PoseLandmarkerHelper.LandmarkerListener { // Listener 구현
    private fun idToIndex(id: Long): Int =
        when (id.toInt()) {
            1 -> 4
            2 -> 2
            3 -> 3
            4 -> 6
            5 -> 1
            6 -> 5
            7 -> 0
            else->0
        }
    lateinit var currentPose: YogaPose
    private val _rawAccuracy = MutableStateFlow(0f)
    val displayAccuracy = _rawAccuracy
        .sample(300)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0f
        )
    var bestAccuracy = 0f
    var remainingPoseTime = 0F // 최고 포즈 유지 시간
    var bestResultBitmap: Bitmap? = null
    private var lastProcessedFrameTimestampMs = -1L

//    private val _finalPoseResult = MutableStateFlow<FinalPoseResult?>(null)
//    val finalPoseResult: StateFlow<FinalPoseResult?> = _finalPoseResult.asStateFlow()



    val imageAnalyzerExecutor: ExecutorService
        get() = cameraExecutor

    private val _currentIdx = MutableStateFlow(0)
    val currentIdx: StateFlow<Int> = _currentIdx.asStateFlow()

    private val _isHelperReady = MutableStateFlow(false)
    val isHelperReady: StateFlow<Boolean> = _isHelperReady.asStateFlow()

    private val _poseResult = MutableStateFlow<PoseLandmarkerHelper.ResultBundle?>(null)
    val poseResult: StateFlow<PoseLandmarkerHelper.ResultBundle?> = _poseResult.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // ImageAnalysis에 사용할 Executor
    private lateinit var cameraExecutor: ExecutorService

    // ImageAnalysis UseCase에 제공할 분석기 인스턴스
    val imageAnalyzer: ImageAnalysis.Analyzer

    // 현재 카메라 렌즈 방향 (UI에서 설정하거나 기본값 사용)
    // CameraPreview에서 LENS_FACING_FRONT를 사용하므로 true로 가정
    private var isFrontCamera = true

    private val _isAnalysisPaused = MutableStateFlow(false) // 또는 초기 상태에 맞게
    val isAnalysisPaused: StateFlow<Boolean> = _isAnalysisPaused.asStateFlow()

    fun setAnalysisPaused(paused: Boolean) {
        _isAnalysisPaused.value = paused
        if(!paused){
            lastProcessedFrameTimestampMs = System.currentTimeMillis()
        }
    }


    init {
        // ... (Executor 초기화, imageAnalyzer 초기화) ...
        cameraExecutor = Executors.newSingleThreadExecutor()
        imageAnalyzer = ImageAnalysis.Analyzer { imageProxy ->
            if (isAnalysisPaused.value) {
                imageProxy.close()
                return@Analyzer
            }
            if (!_isHelperReady.value) {
                imageProxy.close()
                return@Analyzer
            }
            poseLandmarkerHelper.detectLiveStream(imageProxy, isFrontCamera)
        }


        // *** ViewModel 생성 시점에 Listener를 먼저 할당 ***
        poseLandmarkerHelper.poseLandmarkerHelperListener = this
        Log.d("CameraViewModel", "Listener assigned to PoseLandmarkerHelper.") // 로그 추가

        // *** 그 다음에 Helper 설정 시작 ***
        setupHelper()
    }
    fun initPose(pose:YogaPose){
        currentPose = pose
        _currentIdx.value = idToIndex(pose.poseId)
        _rawAccuracy.value = 0f
        bestAccuracy = 0f
        remainingPoseTime = 0F
        bestResultBitmap = null
        lastProcessedFrameTimestampMs = -1L
    }

    private fun setupHelper() {
        // Listener 할당은 init 블록에서 이미 수행했으므로 여기서는 제거
        // poseLandmarkerHelper.poseLandmarkerHelperListener = this // 제거

        // Helper 초기화 실행 (백그라운드)
        viewModelScope.launch {
            Log.d("CameraViewModel", "Launching setupPoseLandmarker coroutine...") // 로그 추가
            poseLandmarkerHelper.setupPoseLandmarker() // 이제 Listener는 null이 아닐 것임

            // 초기화 성공/실패에 따른 상태 업데이트
            _isHelperReady.value = !poseLandmarkerHelper.isClose()
            if (_isHelperReady.value) {
                Log.d("CameraViewModel", "PoseLandmarkerHelper setup finished successfully.")
            } else {
                Log.e("CameraViewModel", "PoseLandmarkerHelper setup failed (remains closed). Check onError logs.")
                // onError 콜백이 이미 에러 상태를 설정했을 수 있음
            }
        }
    }

    // PoseLandmarkerHelper.LandmarkerListener 구현
    override fun onError(error: String, errorCode: Int) {
        viewModelScope.launch {
            _error.value = "Pose Landmarker Error: $error (Code: $errorCode)"
            _isHelperReady.value = false // 에러 발생 시 준비 안된 상태로 간주
            Log.e("CameraViewModel", "OnError received: $error, Code: $errorCode")
            // 에러 발생 시 UI 피드백 로직 추가 가능
        }
    }

    override fun onResults(resultBundle: PoseLandmarkerHelper.ResultBundle) {
        // <<--- IO 또는 Default Dispatcher 사용! --- >>
        viewModelScope.launch(Dispatchers.IO) {
            // ... (기존 로직 대부분 여기로 이동) ...
            val img = resultBundle.image // 백그라운드에서 이미지 사용
            _poseResult.value=resultBundle

            val keypointsArray = resultBundle.results.firstOrNull()?.landmarks()?.firstOrNull()?.let { landmarks ->
                val keypoints = extractKeypointsFromLandmarks(landmarks)
                normalizeKeypoints(keypoints)
            }

            if (keypointsArray != null && img != null) {
                Log.d("CameraViewModel", "Running inference (BG Thread) with keypoints size: ${keypointsArray.size}")
                // runInference는 이제 백그라운드에서 실행됨
                val result = bestPoseModelUtil.runInference(keypointsArray, img)

                result?.let { bestModelResult ->
                    val accuracyArray = bestModelResult.first
                    val inferenceTime = bestModelResult.second // TFLite 추론 시간

                    accuracyArray?.let {
                        Log.d("CameraViewModel", "Inference result: ${it.contentToString()}, Time: ${inferenceTime}ms")

                        val currentTimestampMs = System.currentTimeMillis()
                        // 이 totalInferenceTime은 MediaPipe + TFLite + 중간 처리 시간 포함
                        // 좀 더 정확히 하려면 MediaPipe 시작 시간부터 측정 필요
                        val totalProcessingTime = currentTimestampMs - lastProcessedFrameTimestampMs
                        Log.d("CameraViewModel", "Total Processing Time (since last frame processed): ${totalProcessingTime}ms")

                        val accuracy = it[currentIdx.value] // currentIdx는 Main 스레드 값 접근 시 주의 필요, 필요시 withContext(Main) 사용

                        // UI 업데이트는 Main 스레드로 전환
                        withContext(Dispatchers.Main) {
                            _rawAccuracy.value = accuracy // Main 스레드에서 LiveData 업데이트 시 value 사용
                            if (accuracy >= 0.5f && lastProcessedFrameTimestampMs != -1L) {
                                remainingPoseTime += totalProcessingTime / 1000.0F // Main 스레드에서 업데이트
                            }
                            if (accuracy > bestAccuracy) {
                                // bestResultBitmap 관리 로직 (Main 스레드에서 안전하게 처리)
                                bestResultBitmap?.recycle() // 이전 비트맵 해제
                                // 중요: img는 백그라운드 스레드에서 왔으므로, Main에서 사용하려면 복사본이 안전할 수 있음
                                // 또는 img의 생명주기를 명확히 관리해야 함.
                                // 여기서는 일단 img를 직접 사용하나, 문제가 생기면 복사 고려
                                bestResultBitmap = img
                                bestAccuracy = accuracy
                            } else {
                                // 최고 점수가 아니면 여기서 img 해제
                                img?.recycle() // <<--- 중요: 여기서 해제 필요!
                            }
                            // lastProcessedFrameTimestampMs 업데이트도 Main에서
                            lastProcessedFrameTimestampMs = currentTimestampMs
                        } // end withContext(Dispatchers.Main)
                    } ?: run {
                        // accuracyArray가 null일 때, img 해제 필요
                        img?.recycle()
                    }
                } ?: run {
                    // TFLite 추론 실패 시, img 해제 필요
                    img?.recycle()
                }
            } else {
                Log.w("CameraViewModel", "Skipped inference (BG Thread): keypointsArray=${keypointsArray != null}, img=${img != null}")
                // 추론 건너뛸 때도 img 해제 필요
                img?.recycle()
            }

            // _isHelperReady 업데이트도 Main 스레드에서 하는 것이 안전
            if (!_isHelperReady.value) {
                withContext(Dispatchers.Main) { _isHelperReady.value = true }
            }
        } // end viewModelScope.launch(Dispatchers.IO)
    }

    fun normalizeKeypoints(
        keypoints: List<Keypoint>,
        imageWidth: Int = 480,
        imageHeight: Int = 480,
        visibilityThreshold: Float = 0.3f,
        maskValue: Float = -1.0f
    ): FloatArray {
        // 어깨(11, 12), 골반(23, 24)
        val s1 = keypoints[11]
        val s2 = keypoints[12]
        val h1 = keypoints[23]
        val h2 = keypoints[24]

        val allVisible = listOf(s1, s2, h1, h2).all { it.visibility >= visibilityThreshold }

        val center: Pair<Float, Float>
        val scale: Float

        if (allVisible) {
            val x11 = s1.x * imageWidth
            val y11 = s1.y * imageHeight
            val x12 = s2.x * imageWidth
            val y12 = s2.y * imageHeight
            val x23 = h1.x * imageWidth
            val y23 = h1.y * imageHeight
            val x24 = h2.x * imageWidth
            val y24 = h2.y * imageHeight

            val shoulderCenter = Pair((x11 + x12) / 2f, (y11 + y12) / 2f)
            val hipCenter = Pair((x23 + x24) / 2f, (y23 + y24) / 2f)
            center = Pair((shoulderCenter.first + hipCenter.first) / 2f,
                (shoulderCenter.second + hipCenter.second) / 2f)

            scale = kotlin.math.sqrt(
                (shoulderCenter.first - hipCenter.first).pow(2) +
                        (shoulderCenter.second - hipCenter.second).pow(2)
            ) + 1e-6f
        } else {
            center = Pair(0f, 0f)
            scale = 1f
        }

        // 정규화된 keypoint 벡터 생성
        val vector = FloatArray(33 * 3)

        for (i in 0 until 33) {
            val kp = keypoints[i]
            val v = kp.visibility
            val x = kp.x * imageWidth
            val y = kp.y * imageHeight

            if (v < visibilityThreshold || !allVisible) {
                vector[i * 3] = maskValue
                vector[i * 3 + 1] = maskValue
                vector[i * 3 + 2] = v
            } else {
                vector[i * 3] = (x - center.first) / scale
                vector[i * 3 + 1] = (y - center.second) / scale
                vector[i * 3 + 2] = v
            }
        }

        return vector
    }
//    fun normalizeKeypoints(
//        keypoints: List<Keypoint>,  // (x, y, visibility)
//        imageWidth: Int = 480,
//        imageHeight: Int = 480,
//        visibilityThreshold: Float = 0.5f,
//        maskValue: Float = -1.0f,
//        minScale: Float = 20.0f
//    ): FloatArray {
//        val vector = mutableListOf<Float>()
//
//        val (x11, y11, v11) = keypoints[11]
//        val (x12, y12, v12) = keypoints[12]
//
//        val px11 = x11 * imageWidth
//        val py11 = y11 * imageHeight
//        val px12 = x12 * imageWidth
//        val py12 = y12 * imageHeight
//
//        val (xCenter, yCenter, scale) = if (v11 >= visibilityThreshold && v12 >= visibilityThreshold) {
//            val cx = (px11 + px12) / 2f
//            val cy = (py11 + py12) / 2f
//            val dist = Math.hypot((px11 - px12).toDouble(), (py11 - py12).toDouble()).toFloat()
//            Triple(cx, cy, maxOf(dist, minScale))
//        } else {
//            Triple(null, null, null)
//        }
//
//        for ((x, y, v) in keypoints) {
//            val px = x * imageWidth
//            val py = y * imageHeight
//            if (v < visibilityThreshold || xCenter == null || yCenter == null || scale == null) {
//                vector.add(maskValue)
//                vector.add(maskValue)
//                vector.add(v)
//            } else {
//                val xOut = (px - xCenter) / scale
//                val yOut = (py - yCenter) / scale
//                vector.add(xOut)
//                vector.add(yOut)
//                vector.add(v)
//            }
//        }
//
//        return vector.toFloatArray()
//    }

//    fun preprocessKeypointsWithPixels(
//        keypoints: List<Keypoint>,
//        imageWidth: Int = 224,
//        imageHeight: Int = 224,
//        visibilityThreshold: Float = 0.5f,
//        maskValue: Float = -1.0f
//    ): FloatArray {
//        val vector = mutableListOf<Float>()
//
//        val x11 = keypoints[11].x * imageWidth
//        val x12 = keypoints[12].x * imageWidth
//        val y11 = keypoints[11].y * imageHeight
//        val y12 = keypoints[12].y * imageHeight
//        val v11 = keypoints[11].visibility
//        val v12 = keypoints[12].visibility
//
//        val (xCenter, yCenter, scale) = if (v11 >= visibilityThreshold && v12 >= visibilityThreshold) {
//            val centerX = (x11 + x12) / 2f
//            val centerY = (y11 + y12) / 2f
//            val shoulderDist = kotlin.math.sqrt((x11 - x12).pow(2) + (y11 - y12).pow(2)) + 1e-6f
//            Triple(centerX, centerY, shoulderDist)
//        } else {
//            Triple(null, null, null)
//        }
//
//        for (i in 0 until 33) {
//            val x = keypoints[i].x * imageWidth
//            val y = keypoints[i].y * imageHeight
//            val v = keypoints[i].visibility
//
//            if (v < visibilityThreshold || xCenter == null || scale == null) {
//                vector.add(maskValue)
//                vector.add(maskValue)
//                vector.add(v)
//            } else {
//                val normX = (x - xCenter) / scale
//                val normY = (y - (yCenter?:0f)) / scale
//                vector.add(normX)
//                vector.add(normY)
//                vector.add(v)
//            }
//        }
//
//        return vector.toFloatArray()
//    }
    fun extractKeypointsFromLandmarks(landmarks: List<NormalizedLandmark>): List<Keypoint> {
        return landmarks.map { lm ->
            Keypoint(
                x = lm.x(),
                y = lm.y(),
                visibility = lm.visibility().get() // 또는 lm.presence
            )
        }
    }



    // ViewModel 소멸 시 리소스 해제
    override fun onCleared() {
        super.onCleared()
        poseLandmarkerHelper.clearPoseLandmarker()
        if (::cameraExecutor.isInitialized && !cameraExecutor.isShutdown) {
            cameraExecutor.shutdown()
        }
        Log.d("CameraViewModel", "ViewModel cleared, resources released.")
    }

}