package com.d104.yogaapp.features.multi.play

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d104.domain.model.GameStateMessage
import com.d104.domain.model.IceCandidateMessage
import com.d104.domain.model.ImageChunkMessage
import com.d104.domain.model.PeerUser
import com.d104.domain.model.ScoreUpdateMessage
import com.d104.domain.model.UserJoinedMessage
import com.d104.domain.usecase.CloseWebRTCUseCase
import com.d104.domain.usecase.CloseWebSocketUseCase
import com.d104.domain.usecase.ConnectWebSocketUseCase
import com.d104.domain.usecase.GetUserIdUseCase
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
import com.d104.yogaapp.utils.bitmapToBase64
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MultiPlayViewModel @Inject constructor(
    private val multiPlayReducer: MultiPlayReducer,
    private val connectWebSocketUseCase: ConnectWebSocketUseCase,
    private val closeWebSocketUseCase: CloseWebSocketUseCase,
    initializeWebRTCUseCase: InitializeWebRTCUseCase,
    private val closeWebRTCUseCase: CloseWebRTCUseCase,
    private val observeWebRTCMessageUseCase: ObserveWebRTCMessageUseCase,
    private val handleSignalingMessage: HandleSignalingMessage,
    private val sendWebRTCUseCase: SendWebRTCUseCase,
    private val observeWebSocketConnectionStateUseCase: ObserveWebSocketConnectionStateUseCase,
    private val initiateConnectionUseCase: InitiateConnectionUseCase,
    private val sendSignalingMessageUseCase: SendSignalingMessageUseCase,
    private val processChunkImageUseCase: ProcessChunkImageUseCase,
    private val observeChunkImageUseCase: ObserveChunkImageUseCase,
    private val sendImageUseCase: SendImageUseCase,
    private val getUserIdUseCase: GetUserIdUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(MultiPlayState())
    val uiState: StateFlow<MultiPlayState> = _uiState.asStateFlow()
    private var currentTimerStep: Float = 1f
    private val totalTimeMs = 20_000L //테스트용 5초
    private var timerJob: Job? = null
    private val intervalMs = 100L // 0.1초마다 업데이트
    private val totalSteps = totalTimeMs / intervalMs
    private fun startTimer() {
        if (!uiState.value.isPlaying || !uiState.value.cameraPermissionGranted || uiState.value.currentRoom!!.userCourse.tutorial) return

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            // 현재 진행 상태에 맞는 단계 계산
            val startStep = (currentTimerStep * totalSteps).toInt()

            // 현재 단계부터 카운트다운 시작
            for (step in startStep downTo 0) {
                val progress = step.toFloat() / totalSteps
                processIntent(MultiPlayIntent.UpdateTimerProgress(progress))
                delay(intervalMs)

                if (!uiState.value.isPlaying || !uiState.value.cameraPermissionGranted || uiState.value.isCountingDown) {
                    break
                }
            }

            // 타이머 종료 후 다음 동작으로 자동 전환
//            if (state.value.timerProgress <= 0f) {
////                if(state.value.isLogin&&!state.value.userCourse.tutorial){
////                    val currentidx = state.value.currentPoseIndex
////                    Timber.d("history:${state.value.poseHistories}")
////                }
//                processIntent(SoloYogaPlayIntent.GoToNextPose)
//            }
        }
    }

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

            is MultiPlayIntent.ReceiveWebSocketMessage -> {
                if (intent.message.type == "round_end") {
                    sendScore()
                }
                if (intent.message.type == "image") {
                    sendImage()
                }
                if (intent.message.type == "user_joined") {
                    Timber.d("User joined: ${intent.message}")
                    val peerId = (intent.message as UserJoinedMessage).fromPeerId
                    val nickname = intent.message.userNickName
                    if (!uiState.value.userList.keys.contains(peerId)) {
                        processIntent(MultiPlayIntent.UserJoined(PeerUser(
                            id = peerId,
                            nickName = nickname,
                            isReady = false,
                            totalScore = 0,
                            roundScore = 0.0f
                        )))
                        sendJoinMessage()
                    } else {
                        Timber.d("User already joined: $peerId")
                    }
                    if (uiState.value.currentRoom!!.roomMax == uiState.value.userList.size) {
                        Timber.d("Game started")
                        sendStartMessage()
                    }
                }
                if (intent.message.type == "game_state") {
                    Timber.d("Game state: ${intent.message}")
                    //state = yoga의 index 해당하는 index로 게임 라운드 진행.
                    val state = (intent.message as GameStateMessage).state
                    if (state == 0) {
                        Timber.d("Game started")
                        processIntent(MultiPlayIntent.GameStarted)
                        initiateMeshNetwork()
                    } else if (state >= 1) {
                        Timber.d("Round $state started")
                        processIntent(MultiPlayIntent.RoundStarted(state))
                    }
                    startTimer()
                }
                if (intent.message.type == "user_left") {
                    processIntent(MultiPlayIntent.UserLeft(intent.message.fromPeerId))
                }
            }

            is MultiPlayIntent.ExitRoom -> {
                // 방 나가기 처리
                viewModelScope.launch {
                    val myId = getUserIdUseCase()
                    if(sendSignalingMessageUseCase(
                        myId,
                        uiState.value.currentRoom!!.roomId.toString(), 3
                    )){
                        Timber.d("User left: $myId")
                        closeWebSocketUseCase()
                        closeWebRTCUseCase()
                    } else {
                        Timber.d("Failed to send user left message")
                    }
                }
            }

            else -> {}
        }
    }


    private fun sendJoinMessage() {
        viewModelScope.launch {
            val id = getUserIdUseCase()
            sendSignalingMessageUseCase(
                id,
                uiState.value.currentRoom!!.roomId.toString(),
                0
            )
        }
    }

    private fun sendStartMessage() {
        viewModelScope.launch {
            val id = getUserIdUseCase()
            if (uiState.value.gameState == GameState.Waiting && id.toLong() == uiState.value.currentRoom!!.userId) {
                Timber.d("Sending start message")
                sendSignalingMessageUseCase(
                    id,
                    uiState.value.currentRoom!!.roomId.toString(),
                    4
                )
            }
        }
    }

    private fun sendImage() {
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

    private fun sendScore() {
        viewModelScope.launch {
            sendWebRTCUseCase(
                message = ScoreUpdateMessage(
                    score = uiState.value.second,
                    time = uiState.value.second
                )
            )
        }
    }

    private fun initiateMeshNetwork() {
        viewModelScope.launch {
            val myId = getUserIdUseCase()
            uiState.value.userList.forEach {
                if (it.key < myId)
                    initiateConnectionUseCase(myId, it.value.id)
            }
        }
    }

    init {

        initializeWebRTCUseCase()

        viewModelScope.launch {
            try {
                val roomId = uiState.map { it.currentRoom }
                    .filterNotNull() // currentRoom이 null이 아닐 때까지 기다림
                    .first()        // 첫 번째 non-null 값 사용
                    .roomId.toString()

                Timber.d("Room ID acquired: $roomId. Setting up WebSocket connection and observation.")

                // 연결 상태 관찰 및 Join 메시지 전송 (연결 시도와 함께 관리)
                // observeWebSocketConnectionStateUseCase가 connectWebSocketUseCase 내부의 상태를 반영한다고 가정합니다.
                // 별도의 launch를 사용하여 상태 변화를 감지하고 Join 메시지를 한 번만 보냅니다.
                launch { // 상태 관찰 및 Join 메시지 전송을 위한 별도 코루틴
                    observeWebSocketConnectionStateUseCase()
                        .filter { it == StompConnectionState.CONNECTED } // CONNECTED 상태 필터링
                        .first() // 첫 번째 CONNECTED 상태가 되면 아래 블록 실행하고 종료
                        .let {
                            sendJoinMessage()
                        }
                }

                Timber.d("Calling connectWebSocketUseCase for room $roomId.")
                connectWebSocketUseCase(roomId).collect { msg ->
                    Timber.v("Received WebSocket message: Type=${msg.type}") // 메시지 수신 로그 추가
                    // 시그널링 메시지 처리
                    if (msg.type == "candidate") {
                        val message = msg as IceCandidateMessage
                        Timber.v("Received IceCandidateMessage: from: ${message.fromPeerId} to: ${message.toPeerId}")
                    }
                    handleSignalingMessage(msg)
                    // 기타 메시지 처리 (Intent 사용)
                    processIntent(MultiPlayIntent.ReceiveWebSocketMessage(msg))
                }

            } catch (e: Exception) {
                // roomId를 가져오거나, 연결하거나, 메시지 수집 중 발생하는 모든 예외 처리
                Timber.e(
                    e,
                    "Error during WebSocket setup or message collection for room ${uiState.value.currentRoom?.roomId}"
                )
                // TODO: 사용자에게 오류 알림 또는 상태 업데이트
                // processIntent(MultiPlayIntent.ShowError("WebSocket connection failed"))
            }
        }

        viewModelScope.launch {
            observeWebRTCMessageUseCase().collect {
                launch(Dispatchers.IO) { // 또는 Dispatchers.Default
                    when (it.second) {
                        is ImageChunkMessage -> {
                            processChunkImageUseCase((it.second as ImageChunkMessage))
//                            processIntent(MultiPlayIntent.ReceiveWebRTCImage(base64ToBitmap((it.second as ImageChunkMessage).dataBase64)!!))
                        }

                        is ScoreUpdateMessage -> {
                            processIntent(
                                MultiPlayIntent.UpdateScore(
                                    it.first,
                                    (it.second as ScoreUpdateMessage)
                                )
                            )
                        }

                    }
                }
            }
        }

        viewModelScope.launch {
            observeChunkImageUseCase().collect {
                withContext(Dispatchers.IO) {
                    val bitmap = try {
                        BitmapFactory.decodeByteArray(it, 0, it.size)
                    } catch (e: Exception) { /* ... */
                    }
                    bitmap?.let {
                        processIntent(MultiPlayIntent.ReceiveWebRTCImage(bitmap as Bitmap))
                    }
                }
            }
        }
    }
}
//