package com.d104.yogaapp.features.solo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class SoloYogaViewModel @Inject constructor(
    private val reducer: SoloYogaPlayReducer
) : ViewModel() {

    private val _state = MutableStateFlow(SoloYogaPlayState())
    val state: StateFlow<SoloYogaPlayState> = _state.asStateFlow()

    private var timerJob: Job? = null

    init {
        startTimer()
    }

    fun processIntent(intent: SoloYogaPlayIntent) {
        val newState = reducer.reduce(state.value, intent)
        _state.value = newState

        // 특정 Intent에 대한 부수 효과 처리
        when (intent) {
            is SoloYogaPlayIntent.TogglePlayPause -> handlePlayPauseChange(newState.isPlaying)
            is SoloYogaPlayIntent.SkipPose -> startTimer()
            else -> {} // 다른 Intent는 추가 처리 없음
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
        if (!state.value.isPlaying) return

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            val totalTimeMs = 20_000L // 20초
            val intervalMs = 100L // 0.1초마다 업데이트
            val totalSteps = totalTimeMs / intervalMs

            for (step in totalSteps downTo 0) {
                val progress = step.toFloat() / totalSteps
                processIntent(SoloYogaPlayIntent.UpdateTimerProgress(progress))
                delay(intervalMs)

                if (!state.value.isPlaying) {
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