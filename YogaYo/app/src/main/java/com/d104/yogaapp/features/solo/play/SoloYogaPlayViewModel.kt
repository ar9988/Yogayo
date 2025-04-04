package com.d104.yogaapp.features.solo.play

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d104.domain.model.YogaHistory
import com.d104.domain.model.YogaPose
import com.d104.domain.usecase.PostYogaPoseHistoryUseCase
import com.d104.yogaapp.utils.ImageDownloader
import com.d104.yogaapp.utils.ImageStorageManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.random.Random


@HiltViewModel
class SoloYogaPlayViewModel @Inject constructor(
    private val reducer: SoloYogaPlayReducer,
    private val imageStorageManager: ImageStorageManager,
    private val imageDownloader: ImageDownloader,
    private val postYogaPoseHistoryUseCase: PostYogaPoseHistoryUseCase
    ) : ViewModel() {


    private val _state = MutableStateFlow(SoloYogaPlayState())
    val state: StateFlow<SoloYogaPlayState> = _state.asStateFlow()


    val currentPose: StateFlow<YogaPose> = _state.map { state ->
        if (state.userCourse.poses.isNotEmpty() &&
            state.currentPoseIndex < state.userCourse.poses.size) {
            state.userCourse.poses[state.currentPoseIndex]
        } else {
            YogaPose(0, "", "", 0, listOf("나무 자세 설명"), "", 0,"")
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        YogaPose(0, "", "", 0, listOf("나무 자세 설명"), "", 0,"")
    )

    private var timerJob: Job? = null

    private var currentTimerStep: Float = 1f
    private val totalTimeMs = 20_000L //테스트용 5초
//    private val totalTimeMs = 20_000L // 20초
    private val intervalMs = 100L // 0.1초마다 업데이트
    private val totalSteps = totalTimeMs / intervalMs

    fun processIntent(intent: SoloYogaPlayIntent) {
        // TogglePlayPause 인텐트 처리 전에 현재 타이머 상태 저장
        if (intent is SoloYogaPlayIntent.TogglePlayPause) {
            currentTimerStep = state.value.timerProgress
        }

        // Reducer로 새로운 상태 생성
        val newState = reducer.reduce(state.value, intent)

        // 상태 업데이트
        _state.value = newState

        // 특정 Intent에 대한 부수 효과 처리
        when (intent) {
            is SoloYogaPlayIntent.TogglePlayPause -> {
                handlePlayPauseChange(newState.isPlaying)
            }
            is SoloYogaPlayIntent.GoToNextPose -> {

                Timber.d("current pose id : ${currentPose.value.poseId}")

            }
            is SoloYogaPlayIntent.RestartCurrentPose -> {
                // 현재 포즈 다시 시작
                currentTimerStep = 1f
                startTimer()
            }
            is SoloYogaPlayIntent.UpdateCameraPermission -> {
                if (intent.granted && state.value.isPlaying) {
                    // 권한이 부여되고 재생 상태인 경우에만 타이머 시작
                    startTimer()
                }
            }
            is SoloYogaPlayIntent.UpdateTimerProgress -> {
                // 타이머 진행 상황 업데이트 시 현재 단계 저장
                currentTimerStep = intent.progress
            }
            is SoloYogaPlayIntent.Exit -> {
            }

            is SoloYogaPlayIntent.InitializeWithCourse -> {


            }

            is SoloYogaPlayIntent.SendHistory -> {
                if(!state.value.isSkipped) {
                    viewModelScope.launch {
                        val uri = saveImage(intent.bitmap, currentPose.value)
                        uri?.let { savedUri ->
                            updatePoseHistory(
                                pose = intent.pose,
                                accuracy = intent.accuracy,
                                time = intent.time,
                                imageUri = savedUri,
                            )
                        }
                    }
                }else {
                    _state.update {it.copy(isSkipped = false)  }
                }
            }

            SoloYogaPlayIntent.ExitGuide -> {
                currentTimerStep = 1f
                startTimer()
            }

            SoloYogaPlayIntent.FinishCountdown -> {
                startTimer()

            }
            SoloYogaPlayIntent.StartCountdown -> {


            }

            SoloYogaPlayIntent.SkipPose -> {
                Timber.d("current pose id : ${currentPose.value.poseId}")
            }

            is SoloYogaPlayIntent.DownloadImage ->{
                downloadImage(intent.uri,intent.poseName)
            }
            SoloYogaPlayIntent.ResetDownloadState -> {
            }

            is SoloYogaPlayIntent.SetLoginState -> {}
            is SoloYogaPlayIntent.SetCurrentHistory -> {}
        }
    }

    private fun handlePlayPauseChange(isPlaying: Boolean) {
        if (isPlaying) {
            startTimer()
        } else {
            timerJob?.cancel()
        }
    }

    private fun startTimer() {
        if (!state.value.isPlaying || !state.value.cameraPermissionGranted||state.value.userCourse.tutorial) return

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            // 현재 진행 상태에 맞는 단계 계산
            val startStep = (currentTimerStep * totalSteps).toInt()

            // 현재 단계부터 카운트다운 시작
            for (step in startStep downTo 0) {
                val progress = step.toFloat() / totalSteps
                processIntent(SoloYogaPlayIntent.UpdateTimerProgress(progress))
                delay(intervalMs)

                if (!state.value.isPlaying || !state.value.cameraPermissionGranted||state.value.isCountingDown) {
                    break
                }
            }

            // 타이머 종료 후 다음 동작으로 자동 전환
            if (state.value.timerProgress <= 0f) {
//                if(state.value.isLogin&&!state.value.userCourse.tutorial){
//                    val currentidx = state.value.currentPoseIndex
//                    Timber.d("history:${state.value.poseHistories}")
//                }
                processIntent(SoloYogaPlayIntent.GoToNextPose)
            }
        }
    }


    suspend fun saveImage(bitmap: Bitmap,pose:YogaPose): Uri? {
        // 이미지 저장 후 URI 반환
        val imageUri = imageStorageManager.saveImage(bitmap, state.value.currentPoseIndex.toString(), pose.poseId.toString())
        return imageUri
    }

    private fun updatePoseHistory(pose:YogaPose,accuracy:Float,time:Float,imageUri: Uri) {
        // 현재 상태와 인덱스 가져오기
        val currentState = _state.value
        val currentIndex = currentState.currentPoseIndex

        // 현재 포즈 히스토리 리스트 복사
        val updatedHistories = currentState.poseHistories.toMutableList()

        // 새 히스토리 객체 생성
        val newHistory = YogaHistory(
            poseId = pose.poseId,
            poseName = pose.poseName,
            poseTime = time,
            accuracy = accuracy,
            recordImg = imageUri.toString(),
            poseImg = pose.poseImg
        )

        updatedHistories.add(newHistory)

        // 상태 업데이트
        _state.value = currentState.copy(poseHistories = updatedHistories)
        if(state.value.isLogin&&!_state.value.userCourse.tutorial){
            viewModelScope.launch {
                state.value.poseHistories.last().let {history->
                    postYogaPoseHistoryUseCase(
                        poseId = history.poseId,
                        accuracy = history.accuracy,
                        poseTime = history.poseTime,
                        imgUri = history.recordImg
                    ).collectLatest {
                        Timber.d("historyresult:${it}")
                    }

                }

            }

        }

        // 로그 출력 (디버깅용)
        Timber.d("포즈 히스토리 업데이트: 인덱스=$currentIndex, 포즈ID=${pose.poseId}, 이미지=$imageUri")
    }

    fun downloadImage(imageUri: Uri, poseName: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(downloadState = DownloadState.Loading)
            try {
                val success = imageDownloader.saveImageToGallery(imageUri.toString(), poseName)
                _state.value = if (success) _state.value.copy(downloadState = DownloadState.Loading) else _state.value.copy(downloadState = DownloadState.Error("저장 실패"))
            } catch (e: Exception) {
                _state.value = _state.value.copy(downloadState = DownloadState.Error("저장 실패"))
            }
        }
    }

    suspend fun cleanupAndExit(): Boolean {
        return imageStorageManager.deleteAllImages()
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}