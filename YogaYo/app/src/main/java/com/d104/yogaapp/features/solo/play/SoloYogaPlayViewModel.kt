package com.d104.yogaapp.features.solo.play

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject


@HiltViewModel
class SoloYogaPlayViewModel @Inject constructor(
    private val reducer: SoloYogaPlayReducer
) : ViewModel() {

    private val _state = MutableStateFlow(SoloYogaPlayState())
    val state: StateFlow<SoloYogaPlayState> = _state.asStateFlow()

    private var timerJob: Job? = null

    private var currentTimerStep: Float = 1f
    private val totalTimeMs = 20_000L // 20초
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
            is SoloYogaPlayIntent.SkipPose -> {
                // 다음 포즈로 넘어갈 때는 타이머를 처음부터 시작
                currentTimerStep = 1f
                startTimer()
            }
            is SoloYogaPlayIntent.RestartCurrentPose -> {
                // 현재 포즈 다시 시작
                currentTimerStep = 1f
                startTimer()
            }
            is SoloYogaPlayIntent.UpdateCameraPermission -> {
                if (intent.granted && newState.isPlaying) {
                    // 권한이 부여되고 재생 상태인 경우에만 타이머 시작
                    startTimer()
                }
            }
            is SoloYogaPlayIntent.UpdateTimerProgress -> {
                // 타이머 진행 상황 업데이트 시 현재 단계 저장
                currentTimerStep = intent.progress
            }
            is SoloYogaPlayIntent.Exit -> {
                // 종료 인텐트는 Composable에서 처리
            }
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
        if (!state.value.isPlaying || !state.value.cameraPermissionGranted) return

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            // 현재 진행 상태에 맞는 단계 계산
            val startStep = (currentTimerStep * totalSteps).toInt()

            // 현재 단계부터 카운트다운 시작
            for (step in startStep downTo 0) {
                val progress = step.toFloat() / totalSteps
                processIntent(SoloYogaPlayIntent.UpdateTimerProgress(progress))
                delay(intervalMs)

                if (!state.value.isPlaying || !state.value.cameraPermissionGranted) {
                    break
                }
            }

            // 타이머 종료 후 다음 동작으로 자동 전환
            if (state.value.timerProgress <= 0f) {
                processIntent(SoloYogaPlayIntent.SkipPose)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}