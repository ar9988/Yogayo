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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
        viewModelScope.launch {
            // 첫 결과 수신 시 helper가 확실히 준비된 것으로 간주 가능
            if (!_isHelperReady.value) {
                _isHelperReady.value = true
                Log.d("CameraViewModel", "First result received, ensuring helper is marked as ready.")
            }
            val img = resultBundle.image
            _poseResult.value = resultBundle
            val results = resultBundle.results

            // 입력 벡터 추출 및 전처리
            val keypointsArray = results.firstOrNull()?.landmarks()?.firstOrNull()?.let { landmarks ->
                val keypoints = extractKeypointsFromLandmarks(landmarks)
                preprocessKeypointsWithPixels(keypoints)
            }

            // 키포인트와 이미지가 유효한 경우에만 추론 실행
            if (keypointsArray != null && img != null) {
                Log.d("CameraViewModel", "Running inference with keypoints size: ${keypointsArray.size}")
                val result = bestPoseModelUtil.runInference(keypointsArray, img)

                // 결과 처리
                result?.let {bestModelResult->
                    val accuracyArray  = bestModelResult.first
                    accuracyArray?.let{
                        Log.d("CameraViewModel", "Inference result: ${accuracyArray.contentToString()}")
                        val currentTimestampMs = System.currentTimeMillis()
                        val totalInferenceTime = currentTimestampMs-lastProcessedFrameTimestampMs
                        Log.d("CameraViewModel", "Total inferenceTime: ${totalInferenceTime}")
                        val accuracy = accuracyArray[currentIdx.value]
                        _rawAccuracy.update { accuracy }
                        if(accuracy>=0.5f&&lastProcessedFrameTimestampMs!=-1L){//이부분 나중에 조절하기 지금은 1퍼부더 맞는 동작
                            remainingPoseTime+=totalInferenceTime / 1000.0F
                        }
                        if(accuracy>bestAccuracy){
                            bestResultBitmap?.recycle()
                            bestResultBitmap=img
                            bestAccuracy=accuracy
                        }else{
                            resultBundle.image?.recycle()
                        }

                    }


                }
            } else {
                Log.w("CameraViewModel", "Skipped inference: keypointsArray=${keypointsArray != null}, img=${img != null}")
            }

            _error.value = null // 성공적인 결과 수신 시 이전 에러 메시지 클리어
            lastProcessedFrameTimestampMs = System.currentTimeMillis()
        }


    }

    fun preprocessKeypointsWithPixels(
        keypoints: List<Keypoint>,
        imageWidth: Int = 224,
        imageHeight: Int = 224,
        visibilityThreshold: Float = 0.5f,
        maskValue: Float = -1.0f
    ): FloatArray {
        val vector = mutableListOf<Float>()

        val x11 = keypoints[11].x * imageWidth
        val x12 = keypoints[12].x * imageWidth
        val y11 = keypoints[11].y * imageHeight
        val y12 = keypoints[12].y * imageHeight
        val v11 = keypoints[11].visibility
        val v12 = keypoints[12].visibility

        val (xCenter, yCenter, scale) = if (v11 >= visibilityThreshold && v12 >= visibilityThreshold) {
            val centerX = (x11 + x12) / 2f
            val centerY = (y11 + y12) / 2f
            val shoulderDist = kotlin.math.sqrt((x11 - x12).pow(2) + (y11 - y12).pow(2)) + 1e-6f
            Triple(centerX, centerY, shoulderDist)
        } else {
            Triple(null, null, null)
        }

        for (i in 0 until 33) {
            val x = keypoints[i].x * imageWidth
            val y = keypoints[i].y * imageHeight
            val v = keypoints[i].visibility

            if (v < visibilityThreshold || xCenter == null || scale == null) {
                vector.add(maskValue)
                vector.add(maskValue)
                vector.add(v)
            } else {
                val normX = (x - xCenter) / scale
                val normY = (y - (yCenter?:0f)) / scale
                vector.add(normX)
                vector.add(normY)
                vector.add(v)
            }
        }

        return vector.toFloatArray()
    }
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