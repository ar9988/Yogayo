package com.d104.yogaapp.features.common

import android.content.Context
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
import dagger.hilt.android.qualifiers.ApplicationContext
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
import timber.log.Timber
import java.io.InputStreamReader
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject
import kotlin.math.pow
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets


@HiltViewModel
class CameraViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
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
    private val csvFileName = "keypoints_all_proc2.csv"
    private val csvList = MutableStateFlow<List<FloatArray>>(emptyList())

    private val _rawAccuracy = MutableStateFlow(0f)
    fun isCurrentPoseInitialized(): Boolean {
        // ::currentPose는 KProperty 객체를 반환하며 isInitialized로 확인 가능
        val initialized = ::currentPose.isInitialized
        // Timber.v("Checking if currentPose is initialized: $initialized") // 필요시 로그 추가
        return initialized
    }
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

    private val _feedback = MutableStateFlow<String>("")
    val feedback: StateFlow<String> = _feedback.asStateFlow()

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
        viewModelScope.launch{
            csvList.update{readCsvDataAsFloatArray()}
            Timber.d("csv ${csvList.value}")
        }
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
        resetData()
    }

    fun resetData(){
        _rawAccuracy.value = 0f
        bestAccuracy = 0f
        remainingPoseTime = 0F
        bestResultBitmap?.recycle()
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
                        var score = 0.0f
                        var feedbackResult = ""
                        Timber.d("Score & Accuracy ${accuracy}")
                        if(accuracy>0.7){
                            score = if(currentIdx.value==0)getMaxCombinedScoreWithFlip(csvList.value.get(currentIdx.value),keypointsArray, alpha = 0.9f) else getMaxCombinedScoreWithFlip(csvList.value.get(currentIdx.value),keypointsArray, alpha = 0.75f)
                            Timber.d("NMData ${csvList.value}")
                            Timber.d("Score: ${score}")
                            feedbackResult = getFeedbackByPose(currentIdx.value,keypointsArray)

                        }


                        // UI 업데이트는 Main 스레드로 전환
                        withContext(Dispatchers.Main) {
//                            if(accuracy>0.9){
//                                saveFloatArrayToInternalStorage(keypointsArray,"keypoints_all_proc2.csv")
//                            }
                            _feedback.value = feedbackResult
                            Timber.d("feedback: ${feedback.value}")
                            _rawAccuracy.value = score // Main 스레드에서 LiveData 업데이트 시 value 사용
                            if (score >= 70f && lastProcessedFrameTimestampMs != -1L) {
                                remainingPoseTime += totalProcessingTime / 1000.0F // Main 스레드에서 업데이트
                            }
                            if (score > bestAccuracy) {
                                // bestResultBitmap 관리 로직 (Main 스레드에서 안전하게 처리)
                                bestResultBitmap?.recycle() // 이전 비트맵 해제
                                // 중요: img는 백그라운드 스레드에서 왔으므로, Main에서 사용하려면 복사본이 안전할 수 있음
                                // 또는 img의 생명주기를 명확히 관리해야 함.
                                // 여기서는 일단 img를 직접 사용하나, 문제가 생기면 복사 고려
                                bestResultBitmap = img
                                bestAccuracy = score
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

    suspend fun saveFloatArrayToInternalStorage(
        data: FloatArray, // 입력 타입을 FloatArray로 변경
        fileName: String
    ): Boolean {
        // 파일 I/O는 Dispatchers.IO에서 수행
        return withContext(Dispatchers.IO) {
            // 내부 저장소의 files 디렉토리 경로 가져오기
            val directory = context.filesDir
            val file = File(directory, fileName)

            try {
                // FileOutputStream으로 파일을 열고 (기본: 덮어쓰기 모드)
                // OutputStreamWriter (UTF-8) 와 BufferedWriter를 사용
                FileOutputStream(file).use { fos -> // 두 번째 인자 true 없으면 덮어쓰기
                    OutputStreamWriter(fos, StandardCharsets.UTF_8).use { osw ->
                        BufferedWriter(osw).use { writer ->
                            // 입력받은 FloatArray의 각 요소를 문자열로 변환하고 쉼표로 연결
                            val line = data.joinToString(",") { floatValue ->
                                // 필요하다면 여기서 각 float 값의 포맷을 지정할 수 있습니다.
                                // 예: String.format("%.6f", floatValue) // 소수점 6자리까지
                                floatValue.toString() // 기본 toString 사용
                            }
                            writer.write(line) // 변환된 한 줄 쓰기
                            writer.newLine()   // 줄바꿈 추가 (파일 끝에 빈 줄이 생길 수 있음, 필요 없다면 제거)
                        }
                    }
                }
                Log.i("DataSaver", "Successfully saved FloatArray data to ${file.absolutePath}")
                true // 성공
            } catch (e: IOException) {
                Log.e("DataSaver", "Error writing FloatArray to CSV file '$fileName' in internal storage", e)
                false // 실패
            } catch (e: Exception) {
                Log.e("DataSaver", "An unexpected error occurred while saving FloatArray to '$fileName'", e)
                false // 기타 예외
            }
        }
    }



    suspend fun readCsvDataAsFloatArray(): List<FloatArray> {
        // 파일을 읽는 작업은 I/O 작업이므로 Dispatchers.IO 사용
        return withContext(Dispatchers.IO) {
            // 최종 결과를 저장할 리스트 (FloatArray를 요소로 가짐)
            val data = mutableListOf<FloatArray>()
            try {
                context.assets.open(csvFileName).use { inputStream ->
                    InputStreamReader(inputStream, StandardCharsets.UTF_8).use { reader ->
                        BufferedReader(reader).useLines { lines ->
                            // lines.drop(1): 첫 번째 줄(헤더) 건너뛰기
                            lines.drop(1).forEachIndexed { rowIndex, line ->
                                val stringRow = line.split(',')

                                // 첫 번째 열(파일 이름)을 제외해야 하므로, 최소 2개 이상의 열 필요
                                if (stringRow.size <= 1) {
                                    Log.w("CsvReaderUtil", "Skipping row ${rowIndex + 2}: Insufficient columns after split. Line: '$line'")
                                    return@forEachIndexed // 다음 줄로 이동 (continue 역할)
                                }

                                // 이 행의 Float 값들을 임시로 저장할 리스트
                                val floatListForRow = mutableListOf<Float>()
                                var conversionSuccessful = true // 행 전체의 변환 성공 여부 추적

                                // stringRow.subList(1, stringRow.size): 첫 번째 요소 제외한 나머지
                                for ((colIndex, cellValue) in stringRow.subList(1, stringRow.size).withIndex()) {
                                    try {
                                        // 공백 제거 후 Float으로 변환하여 임시 리스트에 추가
                                        floatListForRow.add(cellValue.trim().toFloat())
                                    } catch (e: NumberFormatException) {
                                        // Float 변환 실패 시 로그 남기고 해당 행 처리 중단
                                        Log.e("CsvReaderUtil", "Failed to convert value '${cellValue.trim()}' to Float at row ${rowIndex + 2}, column ${colIndex + 2}. Skipping entire row.", e)
                                        conversionSuccessful = false
                                        break // 이 행의 나머지 열 처리 중단
                                    } catch (e: Exception) {
                                        // 예상치 못한 다른 오류 처리
                                        Log.e("CsvReaderUtil", "Unexpected error processing cell value '${cellValue.trim()}' at row ${rowIndex + 2}, column ${colIndex + 2}. Skipping entire row.", e)
                                        conversionSuccessful = false
                                        break
                                    }
                                }

                                // 행의 모든 숫자 변환이 성공한 경우에만
                                if (conversionSuccessful) {
                                    // 임시 리스트를 FloatArray로 변환하여 최종 데이터 리스트에 추가
                                    data.add(floatListForRow.toFloatArray())
                                }
                            }
                        }
                    }
                }
            } catch (e: IOException) {
                Log.e("CsvReaderUtil", "Error reading CSV file '$csvFileName' from assets", e)
                throw e // 호출 측에서 처리하도록 예외 다시 던지기
            } catch (e: Exception) {
                Log.e("CsvReaderUtil", "An unexpected error occurred while processing CSV '$csvFileName'", e)
                throw e // 호출 측에서 처리하도록 예외 다시 던지기
            }
            data // 최종 파싱된 List<FloatArray> 반환
        }
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

    fun getReferenceVector(filename: String, referenceMap: Map<String, FloatArray>): FloatArray? {
        return referenceMap[filename]
    }

    fun cosineSimilarity(v1: FloatArray, v2: FloatArray): Float {
        var dot = 0f
        var norm1 = 0f
        var norm2 = 0f
        for (i in v1.indices) {
            if ((i % 3) == 2) continue
            if (v1[i] == -1.0f || v2[i] == -1.0f) continue

            dot += v1[i] * v2[i]
            norm1 += v1[i] * v1[i]
            norm2 += v2[i] * v2[i]
        }
        if (norm1 == 0f || norm2 == 0f) return 0f
        return (dot / Math.sqrt((norm1 * norm2).toDouble()) * 100).toFloat()
    }

    // ✅ L2 거리 기반 점수 계산 함수
    fun l2DistanceScore(v1: FloatArray, v2: FloatArray): Float {
        var sumSq = 0f
        var count = 0
        for (i in v1.indices) {
            if ((i % 3) == 2) continue
            if (v1[i] == -1.0f || v2[i] == -1.0f) continue

            val diff = v1[i] - v2[i]
            sumSq += diff * diff
            count++
        }
        if (count == 0) return 0f
        val avgL2 = Math.sqrt((sumSq / count).toDouble()).toFloat()

        // ✅ 거리 → 점수로 변환 (거리가 멀수록 점수 낮게)
        val score = 100f * Math.exp(-3 * avgL2.toDouble())  // 3은 민감도 조절용
        return score.toFloat().coerceIn(0f, 100f)
    }

    // ✅ 좌우 반전된 벡터 생성 (x좌표만 반전)
    fun flipKeypointsX(vector: FloatArray): FloatArray {
        val flipped = vector.copyOf()
        for (i in 0 until 33) {
            val xIdx = i * 3
            if (flipped[xIdx] != -1.0f) {
                flipped[xIdx] = -flipped[xIdx]
            }
        }
        return flipped
    }

    // ✅ 좌우 반전 고려한 혼합 점수 계산
    fun getMaxCombinedScoreWithFlip(
        vec1: FloatArray,
        vec2: FloatArray,
        alpha: Float = 0.6f  // cosine 비중 높게
    ): Float {
        val cosine1 = cosineSimilarity(vec1, vec2)
        val l2_1 = l2DistanceScore(vec1, vec2)
        val score1 = alpha * cosine1 + (1 - alpha) * l2_1

        val flipped = flipKeypointsX(vec1)
        val cosine2 = cosineSimilarity(flipped, vec2)
        val l2_2 = l2DistanceScore(flipped, vec2)
        val score2 = alpha * cosine2 + (1 - alpha) * l2_2

        return maxOf(score1, score2)
    }

    fun getHalasanaFeedback(kp: FloatArray): String {
        val hipY = averageY(kp, 23, 24)
        val hipX = averageX(kp, 23,24)
        val headY = kp[0 * 3 + 1] // nose
        val headX = kp[0*3+0]
        val footY = averageY(kp, 27, 28)
        val footX = averageX(kp,27,28)
        val handY = averageY(kp, 15, 16)
        val isHeadXBetween = (hipX < headX && headX < footX) || (footX < headX && headX < hipX)
        val areFeetAboveHips = footY < hipY


        if ( !isHeadXBetween || areFeetAboveHips ) return "발을 머리 뒤로 넘겨주세요."
        if (hipY > headY + 0.2f) return "엉덩이를 더 들어주세요."
        if (hipY>handY+0.1) return "팔이 땅에서 떨어지면 안됩니다."
        return ""
    }

    fun getBhujangasanaFeedback(kp: FloatArray): String {
        val shoulderY = averageY(kp, 11, 12)
        val shoulderDiff = if (kp[11*3+1] >= 0 && kp[12*3+1] >= 0)
            Math.abs(kp[11*3+1]-kp[12*3+1])
        else 0f
        val hipY = averageY(kp, 23, 24)
        val kneeY = averageY(kp, 25, 26)
        val elbowAngle = getAngle(kp, 11, 13, 15)

        if(kp[11*3+1] > -1 && kp[12*3+1] > -1 && shoulderDiff > 0.1)
            return "어깨를 땅과 수평이 되게 유지하세요"
        if (elbowAngle < 150f) return "팔을 뻗어주세요."
        if (kneeY < hipY - 0.1f) return "다리를 펴주세요."
        if (hipY - shoulderY < -0.1f ) return "엉덩이가 뜨면 안됩니다."
        return ""
    }

    fun getAdhoMukhaFeedback(kp: FloatArray): String {
        val hipY = averageY(kp, 23, 24)
        val headY = kp[0 * 3 + 1]
        val heelY = averageY(kp, 27, 28)

        if (hipY > 0.6f ) return "엉덩이를 더 들어주세요."
        if (heelY > 0.9f) return "다리를 쭉 뻗어주세요."
        if (headY < hipY ) return "머리를 팔 사이로 넣어주세요."
        return ""
    }

    fun getUstrasanaFeedback(kp: FloatArray): String {
        val ankleY = averageY(kp, 27, 28)
        val handY = averageY(kp, 15, 16)
        val kneeY = averageY(kp, 25, 26)
        val noseY = kp[0 * 3 + 1]

        if (kneeY < 0.4f ) return "무릎을 바닥에 붙여주세요."
        if (noseY < 0.3f) return "고개를 더 젖혀주세요."
        if (Math.abs(handY - ankleY) > 0.2f ) return "발목을 잡아주세요."
        return ""
    }

    fun getVirabhadrasana2Feedback(kp: FloatArray): String {
        val wristY = averageY(kp, 15, 16)
        val shoulderY = averageY(kp, 11, 12)
        val kneeAngle = getAngle(kp, 23, 25, 27)
        val ankleDist = if (kp[27*3] >= 0 && kp[28*3] >= 0)
            Math.abs(kp[27*3] - kp[28*3])
        else if (kp[27*3] >= 0) 1f // 충분히 큰 값으로 설정하여 피드백이 발생하지 않게 함
        else if (kp[28*3] >= 0) 1f
        else 0f

        if (Math.abs(wristY - shoulderY) > 0.1f && wristY >= 0 && shoulderY >= 0)
            return "팔 높이를 맞춰주세요."
        if (kneeAngle < 140f) return "무릎을 더 굽혀주세요."
        if (ankleDist < 0.4f) return "다리를 더 벌려주세요."
        return ""
    }

    fun getNavasanaFeedback(kp: FloatArray): String {
        val footY = averageY(kp, 27, 28)
        val hipY = averageY(kp, 23, 24)
        val handY = averageY(kp, 15, 16)

        if (footY > hipY + 0.2f ) return "다리를 더 들어주세요."
        if (handY > hipY + 0.2f) return "팔을 다리에 맞춰주세요."
        return ""
    }

    fun getVirabhadrasana3Feedback(kp: FloatArray): String {
        val footY = averageY(kp, 27, 28)
        val handY = averageY(kp, 15, 16)
        val footX = averageX(kp, 27, 28)
        val hipX = averageX(kp, 23, 24)

        if (Math.abs(footX - hipX) < 0.2f && footX >= 0 && hipX >= 0) return "다리를 펴주세요."
        if (footY > 0.4f) return "다리를 더 들어주세요."
        if (handY > 0.4f) return "팔을 더 들어주세요."
        return ""
    }

    fun averageX(kp: FloatArray, idx1: Int, idx2: Int): Float {
        val x1 = kp[idx1 * 3]
        val x2 = kp[idx2 * 3]

        // 둘 다 유효한 경우 평균 계산
        if (x1 != -1f && x2 != -1f) {
            return (x1 + x2) / 2
        }
        // x1만 유효한 경우
        else if (x1 != -1f) {
            return x1
        }
        // x2만 유효한 경우
        else if (x2 != -1f) {
            return x2
        }
        // 둘 다 유효하지 않은 경우
        return -1f
    }

    fun averageY(kp: FloatArray, idx1: Int, idx2: Int): Float {
        val y1 = kp[idx1 * 3 + 1]
        val y2 = kp[idx2 * 3 + 1]

        // 둘 다 유효한 경우 평균 계산
        if (y1 != -1f && y2 != -1f) {
            return (y1 + y2) / 2
        }
        // y1만 유효한 경우
        else if (y1 != -1f) {
            return y1
        }
        // y2만 유효한 경우
        else if (y2 != -1f) {
            return y2
        }
        // 둘 다 유효하지 않은 경우 (거의 없을 것이라고 가정)
        return -1f
    }

    // ✅ 각도 계산 유틸
    fun getAngle(kp: FloatArray, a: Int, b: Int, c: Int): Float {
        val ax = kp[a * 3]; val ay = kp[a * 3 + 1]
        val bx = kp[b * 3]; val by = kp[b * 3 + 1]
        val cx = kp[c * 3]; val cy = kp[c * 3 + 1]
        val ab = floatArrayOf(ax - bx, ay - by)
        val cb = floatArrayOf(cx - bx, cy - by)
        val dot = ab[0]*cb[0] + ab[1]*cb[1]
        val abLen = Math.sqrt((ab[0]*ab[0] + ab[1]*ab[1]).toDouble())
        val cbLen = Math.sqrt((cb[0]*cb[0] + cb[1]*cb[1]).toDouble())
        val cos = dot / (abLen * cbLen + 1e-6)
        return Math.toDegrees(Math.acos(cos)).toFloat()
    }

    fun getFeedbackByPose(idx: Int, kp: FloatArray): String {
        return when (idx) {
            0 -> getHalasanaFeedback(kp)
            1 -> getNavasanaFeedback(kp)
            2 -> getAdhoMukhaFeedback(kp)
            3 -> getUstrasanaFeedback(kp)
            4 -> getBhujangasanaFeedback(kp)
            5 -> getVirabhadrasana3Feedback(kp)
            6 -> getVirabhadrasana2Feedback(kp)
            else -> "자세를 다시 잡아주세요."
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

    fun getRandomPraise(): String {
        val praises = listOf(
            "잘 하고 있어요!",
            "훌륭해요!",
            "완벽한 자세예요!",
            "정확히 수행하고 있어요!",
            "좋은 자세를 유지하고 있어요!"
        )
        return praises.random()
    }

}