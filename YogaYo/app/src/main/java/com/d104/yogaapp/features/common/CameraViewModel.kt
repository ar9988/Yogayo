package com.d104.yogaapp.features.common

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import android.os.SystemClock
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d104.yogaapp.utils.PoseLandmarkerHelper
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val poseLandmarkerHelper: PoseLandmarkerHelper
) : ViewModel(), PoseLandmarkerHelper.LandmarkerListener { // Listener 구현

    val imageAnalyzerExecutor: ExecutorService
        get() = cameraExecutor

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

    init {
        // ... (Executor 초기화, imageAnalyzer 초기화) ...
        cameraExecutor = Executors.newSingleThreadExecutor()
        imageAnalyzer = ImageAnalysis.Analyzer { imageProxy ->
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

            _poseResult.value = resultBundle
            Log.d("CameraViewModel", "OnResults received: ${resultBundle.results.firstOrNull()?.landmarks()?: 0} landmarks")
            // 결과 처리 로직 추가 (예: 특정 포즈 감지, 상태 업데이트 등)
            _error.value = null // 성공적인 결과 수신 시 이전 에러 메시지 클리어
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

    // --- 설정 변경 메소드 (선택 사항) ---
    fun changeDelegate(newDelegate: Int) {
        viewModelScope.launch {
            _isHelperReady.value = false // 재설정 시작
            poseLandmarkerHelper.currentDelegate = newDelegate
            poseLandmarkerHelper.setupPoseLandmarker() // Helper 재설정
            _isHelperReady.value = !poseLandmarkerHelper.isClose() // 재설정 후 상태 업데이트
        }
    }
    // Confidence 변경 메소드들도 유사하게 구현 가능
}