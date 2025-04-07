package com.d104.yogaapp.utils

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton // 또는 ViewModelScoped, ActivityRetainedScoped 등 필요에 따라

// 필요에 따라 Scope 조정 (@Singleton은 앱 전체에서 하나의 인스턴스만 사용)
// ViewModel과 생명주기를 같이 하려면 @ViewModelScoped 사용 고려 (Hilt 라이브러리 추가 필요)
// 여기서는 간단하게 @Inject constructor 사용 예시
class TtsManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var textToSpeech: TextToSpeech? = null
    private var isTtsInitialized = false
    private var currentScope: CoroutineScope? = null // 외부에서 Scope 주입받거나 자체 생성

    // 현재 발화 중인 텍스트의 인덱스 또는 상태를 외부에 알리기 위한 Flow
    // SharedFlow는 여러 구독자에게 이벤트를 전달하기 좋음
    private val _ttsState = MutableSharedFlow<TtsState>(replay = 1)
    val ttsState = _ttsState.asSharedFlow()

    // TTS 진행 상태 정의
    sealed class TtsState {
        object Idle : TtsState()
        data class Speaking(val type: SpeakType, val index: Int) : TtsState() // index는 description일 때만 의미 있을 수 있음
        object SequenceCompleted : TtsState()
        data class Error(val message: String) : TtsState()
    }

    enum class SpeakType { NAME, DESCRIPTION }

    // TTS 초기화 (비동기 처리 필요)
    suspend fun initialize(scope: CoroutineScope) {
        currentScope = scope
        // 이미 초기화되었거나 초기화 중이면 반환
        if (isTtsInitialized || textToSpeech != null) return

        // TTS 초기화는 메인 스레드에서 이루어져야 할 수 있음 (확인 필요)
        // suspendCancellableCoroutine을 사용하여 콜백 기반 API를 코루틴 친화적으로 만듦
        suspendCancellableCoroutine<Boolean> { continuation ->
            _ttsState.tryEmit(TtsState.Idle) // 초기 상태

            textToSpeech = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    textToSpeech?.language = Locale.getDefault()
                    textToSpeech?.setOnUtteranceProgressListener(ttsListener)
                    isTtsInitialized = true
                    if (continuation.isActive) {
                        continuation.resume(true) {} // 성공 시 true 반환
                    }
                } else {
                    isTtsInitialized = false
                    if (continuation.isActive) {
                        continuation.resume(false) {} // 실패 시 false 반환 (또는 예외 throw)
                    }
                    scope.launch { _ttsState.emit(TtsState.Error("TTS 초기화 실패")) }
                }
            }

            // 코루틴 취소 시 TTS 정리
            continuation.invokeOnCancellation {
                shutdown()
            }
        }
    }

    // 순차적으로 텍스트 읽기
    fun speakSequence(poseName: String, descriptions: List<String>) {
        if (!isTtsInitialized || textToSpeech == null) {
            currentScope?.launch { _ttsState.emit(TtsState.Error("TTS가 준비되지 않았습니다.")) }
            return
        }

        currentScope?.launch(Dispatchers.Main) { // TTS API는 Main 스레드에서 호출하는 것이 안전할 수 있음
            _ttsState.emit(TtsState.Speaking(SpeakType.NAME, -1)) // 이름 읽기 시작 알림
            textToSpeech?.speak(poseName, TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID_NAME)

            descriptions.forEachIndexed { index, description ->
                // QUEUE_ADD를 사용하여 순차적으로 추가
                textToSpeech?.speak(description, TextToSpeech.QUEUE_ADD, null, "$UTTERANCE_ID_DESC_PREFIX$index")
            }
            // 마지막 발화 완료 후 처리를 위한 마커 추가 (선택적)
            // textToSpeech?.speak("", TextToSpeech.QUEUE_ADD, null, UTTERANCE_ID_DONE)
        }
    }

    // TTS 리스너
    private val ttsListener = object : UtteranceProgressListener() {
        override fun onStart(utteranceId: String?) {
            // 발화 시작 시 상태 업데이트
            currentScope?.launch(Dispatchers.Main) {
                when {
                    utteranceId == UTTERANCE_ID_NAME -> {
                        _ttsState.emit(TtsState.Speaking(SpeakType.NAME, -1))
                    }
                    utteranceId?.startsWith(UTTERANCE_ID_DESC_PREFIX) == true -> {
                        val index = utteranceId.substringAfter(UTTERANCE_ID_DESC_PREFIX).toIntOrNull() ?: -1
                        if (index != -1) {
                            _ttsState.emit(TtsState.Speaking(SpeakType.DESCRIPTION, index))
                        }
                    }
                }
            }
        }

        override fun onDone(utteranceId: String?) {
            currentScope?.launch(Dispatchers.Main) {
                // 마지막 설명 발화가 끝났는지 확인
                val lastDescId = "$UTTERANCE_ID_DESC_PREFIX${(textToSpeech?.voices?.size ?: 0) - 1}" // 실제로는 description 리스트 크기로 확인해야 함
                // 정확한 완료 시점 감지를 위해 speakSequence에서 전달받은 description 리스트 크기 필요

                // UtteranceId만으로는 마지막인지 알기 어려움. ViewModel에서 관리하거나, speakSequence에 콜백을 추가하거나,
                // speak 호출 시 파라미터로 마지막임을 알려주는 방법 등을 고려해야 함.
                // 여기서는 단순화를 위해 외부(ViewModel)에서 완료를 판단하도록 상태만 전달하는 방식으로 가정.
                // 또는, onStart에서 다음 발화가 있는지 확인하는 로직을 추가할 수도 있음.

                // 간단한 예시: 마지막 설명 ID 패턴 확인 (개선 필요)
                // if (utteranceId?.startsWith(UTTERANCE_ID_DESC_PREFIX) == true) {
                //     // ViewModel에게 완료 가능성 알림 (ViewModel이 실제 완료 여부 판단)
                // }
                // 혹은 QUEUE_ADD로 모든 항목 추가 후 마지막 항목 onDone 시 완료 처리

                // 임시: 마지막 항목 ID를 특정 값으로 가정 (개선 필요)
                // if (utteranceId == UTTERANCE_ID_DONE) {
                //     _ttsState.emit(TtsState.SequenceCompleted)
                // }

                // 여기서는 완료 시점을 명확히 알기 어렵다는 점을 인지하고,
                // ViewModel에서 상태 변화를 보고 완료를 추론하도록 남겨둡니다.
                // (예: 마지막 설명 index의 Speaking 상태 이후 Idle 상태로 변경되면 완료로 간주)
            }
        }

        override fun onError(utteranceId: String?) {
            currentScope?.launch { _ttsState.emit(TtsState.Error("TTS 발화 오류: $utteranceId")) }
        }

        // Deprecated 되었지만 호환성을 위해 포함할 수 있음
        @Deprecated("Deprecated in Java")
        override fun onError(utteranceId: String?, errorCode: Int) {
            currentScope?.launch { _ttsState.emit(TtsState.Error("TTS 발화 오류: $utteranceId, code: $errorCode")) }
        }
    }

    // TTS 중지
    fun stop() {
        textToSpeech?.stop()
        currentScope?.launch { _ttsState.emit(TtsState.Idle) } // 중지 시 Idle 상태로
    }

    // TTS 리소스 해제
    fun shutdown() {
        stop() // 종료 전 중지
        textToSpeech?.shutdown()
        textToSpeech = null
        isTtsInitialized = false
        // Scope 정리 (ViewModel에서 관리하는 경우 ViewModel의 onCleared에서 호출)
        // currentScope?.cancel() // 외부에서 주입받은 Scope는 여기서 cancel하면 안됨
        currentScope = null
        // 상태 초기화
        _ttsState.tryEmit(TtsState.Idle)
    }

    companion object {
        private const val UTTERANCE_ID_NAME = "pose_name"
        private const val UTTERANCE_ID_DESC_PREFIX = "pose_desc_"
        // private const val UTTERANCE_ID_DONE = "sequence_done" // 완료 마커 ID (선택적)
    }
}