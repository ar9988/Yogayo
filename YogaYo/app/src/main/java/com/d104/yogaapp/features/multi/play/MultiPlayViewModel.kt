package com.d104.yogaapp.features.multi.play

import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d104.domain.model.DataChannelMessage
import com.d104.domain.model.ImageChunkMessage
import com.d104.domain.model.ScoreUpdateMessage
import com.d104.domain.model.SignalingMessage
import com.d104.domain.usecase.CloseWebRTCUseCase
import com.d104.domain.usecase.CloseWebSocketUseCase
import com.d104.domain.usecase.ConnectWebSocketUseCase
import com.d104.domain.usecase.HandleSignalingMessage
import com.d104.domain.usecase.InitializeWebRTCUseCase
import com.d104.domain.usecase.InitiateConnectionUseCase
import com.d104.domain.usecase.ObserveWebRTCMessageUseCase
import com.d104.domain.usecase.ObserveWebSocketConnectionStateUseCase
import com.d104.domain.usecase.SendImageUseCase
import com.d104.domain.usecase.SendSignalingMessageUseCase
import com.d104.domain.usecase.SendWebRTCMessageUseCase
import com.d104.domain.utils.StompConnectionState
import com.d104.yogaapp.utils.base64ToBitmap
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MultiPlayViewModel @Inject constructor(
    private val multiPlayReducer: MultiPlayReducer,
    private val connectWebSocketUseCase: ConnectWebSocketUseCase,
    private val closeWebSocketUseCase: CloseWebSocketUseCase,
    private val initializeWebRTCUseCase: InitializeWebRTCUseCase,
    private val closeWebRTCUseCase: CloseWebRTCUseCase,
    private val observeWebRTCMessageUseCase: ObserveWebRTCMessageUseCase,
    private val handleSignalingMessage: HandleSignalingMessage,
    private val sendWebRTCMessageUseCase: SendWebRTCMessageUseCase,
    private val sendWebRtcImageUseCase: SendImageUseCase,
    private val sendSignalingMessageUseCase: SendSignalingMessageUseCase,
    private val observeWebSocketConnectionStateUseCase:ObserveWebSocketConnectionStateUseCase,
    private val initiateConnectionUseCase: InitiateConnectionUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MultiPlayState())
    val uiState: StateFlow<MultiPlayState> = _uiState.asStateFlow()

    fun processIntent(intent: MultiPlayIntent) {
        val newState = multiPlayReducer.reduce(_uiState.value, intent)
        _uiState.value = newState
        when (intent) {
            is MultiPlayIntent.UpdateCameraPermission -> {
                if (intent.granted) {
                    // 카메라 권한 허용 시
                } else {
                    multiPlayReducer.reduce(_uiState.value, MultiPlayIntent.ExitRoom)
                }
            }

            else -> {}
        }
    }

    override fun onCleared() {
        closeWebSocketUseCase()
        closeWebRTCUseCase()
        super.onCleared()
    }

    private fun initiateMeshNetwork(){
        viewModelScope.launch {
            uiState.value.userList.forEach { it ->
                initiateConnectionUseCase(it.value.id)
            }
        }
    }

    init {

        initializeWebRTCUseCase()
        viewModelScope.launch {
            uiState.map { it.currentRoom }.filterNotNull().first().roomId.let {
                connectWebSocketUseCase(it.toString()).collect { msg ->
                    handleSignalingMessage(msg)
                    processIntent(MultiPlayIntent.ReceiveWebSocketMessage(msg))
                }
            }
        }

        viewModelScope.launch {
            // 1. Room ID 가져오기 (기존 방식 유지, 시점 문제 가능성 유의)
            // 주의: uiState.currentRoom이 초기화 시점에 null이면 first()는 중단될 수 있음.
            //       roomId를 SavedStateHandle 등으로 받는 것이 더 안정적일 수 있음.
            val roomId = uiState.map { it.currentRoom }
                .filterNotNull()
                .first() // currentRoom이 설정될 때까지 기다림
                .roomId.toString()

            // 2. WebSocket 연결 상태 관찰 시작
            observeWebSocketConnectionStateUseCase()
                .filter { it == StompConnectionState.CONNECTED } // 'Connected' 상태 필터링
                .onEach {
                    // 3. 연결 성공 확인 후 "Join" 메시지 전송
                    Timber.d("WebSocket connected! Sending Join message for room $roomId")
                    val success = sendSignalingMessageUseCase(roomId,0)
                    if (!success) {
                        Timber.e("Failed to send Join message for room $roomId")
                    }
                }
                .launchIn(viewModelScope) // 별도의 코루틴에서 관찰 계속 (첫 연결 시 한 번만 실행되도록 하려면 .first() 후 launch) -> first() 사용이 Join을 한 번만 보내는데 더 명확함
            // -> first() 사용 방식으로 수정:
            // connectWebSocketUseCase는 연결 시도 *및* 메시지 Flow 반환을 가정
            try {
                connectWebSocketUseCase(roomId).collect { msg ->
                    if(msg.type=="game_started"){
                        initiateMeshNetwork()
                    }
                    handleSignalingMessage(msg)
                    processIntent(MultiPlayIntent.ReceiveWebSocketMessage(msg))
                }
            } catch (e: Exception) {
                Timber.e(e, "Error collecting WebSocket messages for room $roomId")
                // TODO: WebSocket 메시지 수신 중 오류 처리 (예: 재연결 로직 또는 UI 알림)
            }
        }

        viewModelScope.launch {
            observeWebRTCMessageUseCase().collect {
                launch(Dispatchers.IO) { // 또는 Dispatchers.Default
                    when(it.second){
                        is ImageChunkMessage -> {
                            processIntent(MultiPlayIntent.ReceiveWebRTCImage(base64ToBitmap((it.second as ImageChunkMessage).dataBase64)!!))
                        }
                        is ScoreUpdateMessage -> {
                            processIntent(MultiPlayIntent.UpdateScore(it.first,(it.second as ScoreUpdateMessage)))
                        }
                    }
                }
            }
        }
    }
}