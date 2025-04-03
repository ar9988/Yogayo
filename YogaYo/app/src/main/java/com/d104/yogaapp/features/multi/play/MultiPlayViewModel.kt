package com.d104.yogaapp.features.multi.play

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d104.domain.model.ImageChunkMessage
import com.d104.domain.model.ScoreUpdateMessage
import com.d104.domain.usecase.CloseWebRTCUseCase
import com.d104.domain.usecase.CloseWebSocketUseCase
import com.d104.domain.usecase.ConnectWebSocketUseCase
import com.d104.domain.usecase.HandleSignalingMessage
import com.d104.domain.usecase.InitializeWebRTCUseCase
import com.d104.domain.usecase.InitiateConnectionUseCase
import com.d104.domain.usecase.ObserveChunkImageUseCase
import com.d104.domain.usecase.ObserveWebRTCMessageUseCase
import com.d104.domain.usecase.ObserveWebSocketConnectionStateUseCase
import com.d104.domain.usecase.ProcessChunkImageUseCase
import com.d104.domain.usecase.SendImageUseCase
import com.d104.domain.usecase.SendSignalingMessageUseCase
import com.d104.domain.usecase.SendWebRTCUseCase
import com.d104.domain.utils.StompConnectionState
import com.d104.yogaapp.utils.base64ToBitmap
import com.d104.yogaapp.utils.bitmapToBase64
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    private val sendWebRTCUseCase: SendWebRTCUseCase,
    private val observeWebSocketConnectionStateUseCase:ObserveWebSocketConnectionStateUseCase,
    private val initiateConnectionUseCase: InitiateConnectionUseCase,
    private val sendSignalingMessageUseCase:SendSignalingMessageUseCase,
    private val processChunkImageUseCase: ProcessChunkImageUseCase,
    private val observeChunkImageUseCase: ObserveChunkImageUseCase,
    private val sendImageUseCase: SendImageUseCase
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
            is MultiPlayIntent.ReceiveWebSocketMessage ->{
                if(intent.message.type=="round_end"){
                    sendScore()
                }
                if(intent.message.type=="image"){
                    sendImage()
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

    private fun sendImage(){
        viewModelScope.launch {
            sendImageUseCase(
                params = SendImageUseCase.Params(
                    imageBytes = bitmapToBase64(uiState.value.bitmap!!)!!,
                    targetPeerId = null,
                    quality = 85
                )
            )
        }
    }

    private fun sendScore(){
        viewModelScope.launch {
            sendWebRTCUseCase(
                message = ScoreUpdateMessage(
                    score = uiState.value.second,
                    time = uiState.value.second
                )
            )
        }
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
            try {
                // 1. Room ID 가져오기 (단 한번만)
                // 주의: currentRoom이 초기화 시점에 null이면 first()는 중단될 수 있습니다.
                //       Activity/Fragment로부터 안전하게 전달받는 것이 더 좋습니다 (e.g., SavedStateHandle).
                val roomId = uiState.map { it.currentRoom }
                    .filterNotNull() // currentRoom이 null이 아닐 때까지 기다림
                    .first()        // 첫 번째 non-null 값 사용
                    .roomId.toString()

                Timber.d("Room ID acquired: $roomId. Setting up WebSocket connection and observation.")

                // 2. 연결 상태 관찰 및 Join 메시지 전송 (연결 시도와 함께 관리)
                // observeWebSocketConnectionStateUseCase가 connectWebSocketUseCase 내부의 상태를 반영한다고 가정합니다.
                // 별도의 launch를 사용하여 상태 변화를 감지하고 Join 메시지를 한 번만 보냅니다.
                launch { // 상태 관찰 및 Join 메시지 전송을 위한 별도 코루틴
                    observeWebSocketConnectionStateUseCase()
                        .filter { it == StompConnectionState.CONNECTED } // CONNECTED 상태 필터링
                        .first() // 첫 번째 CONNECTED 상태가 되면 아래 블록 실행하고 종료
                        .let {
                            Timber.i("WebSocket CONNECTED for room $roomId. Sending Join message.")
                            val success = sendSignalingMessageUseCase(roomId, 0) // Join 메시지 전송
                            if (!success) {
                                Timber.e("Failed to send Join message for room $roomId")
                            }
                        }
                    Timber.d("Join message sending logic completed (or state stream ended).")
                }

                // 3. WebSocket 연결 시작 및 메시지 수집 (connectWebSocketUseCase 호출은 여기서 단 한번!)
                Timber.d("Calling connectWebSocketUseCase for room $roomId.")
                connectWebSocketUseCase(roomId).collect { msg ->
                    Timber.v("Received WebSocket message: Type=${msg.type}") // 메시지 수신 로그 추가

                    // game_started 처리
                    if (msg.type == "game_started") {
                        Timber.d("Game started message received. Initiating mesh network.")
                        initiateMeshNetwork()
                    }

                    // 시그널링 메시지 처리
                    handleSignalingMessage(msg)

                    // 기타 메시지 처리 (Intent 사용)
                    processIntent(MultiPlayIntent.ReceiveWebSocketMessage(msg))
                }

            } catch (e: Exception) {
                // roomId를 가져오거나, 연결하거나, 메시지 수집 중 발생하는 모든 예외 처리
                Timber.e(e, "Error during WebSocket setup or message collection for room ${uiState.value.currentRoom?.roomId}")
                // TODO: 사용자에게 오류 알림 또는 상태 업데이트
                // processIntent(MultiPlayIntent.ShowError("WebSocket connection failed"))
            }
        }

        viewModelScope.launch {
            observeWebRTCMessageUseCase().collect {
                launch(Dispatchers.IO) { // 또는 Dispatchers.Default
                    when(it.second){
                        is ImageChunkMessage -> {
                            processChunkImageUseCase((it.second as ImageChunkMessage))
//                            processIntent(MultiPlayIntent.ReceiveWebRTCImage(base64ToBitmap((it.second as ImageChunkMessage).dataBase64)!!))
                        }
                        is ScoreUpdateMessage -> {
                            processIntent(MultiPlayIntent.UpdateScore(it.first,(it.second as ScoreUpdateMessage)))
                        }
                    }
                }
            }
        }

        viewModelScope.launch {
            observeChunkImageUseCase().collect { it ->
                withContext(Dispatchers.IO) {
                    val bitmap = try {
                        BitmapFactory.decodeByteArray(it, 0, it.size)
                    } catch (e: Exception) { /* ... */ }
                    bitmap?.let {
                        processIntent(MultiPlayIntent.ReceiveWebRTCImage(bitmap as Bitmap))
                    }
                }
            }
        }
    }
}