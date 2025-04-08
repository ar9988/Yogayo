package com.d104.yogaapp.features.multi.play

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
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
import com.d104.domain.usecase.PostYogaPoseHistoryUseCase
import com.d104.domain.usecase.ProcessChunkImageUseCase
import com.d104.domain.usecase.SendImageUseCase
import com.d104.domain.usecase.SendSignalingMessageUseCase
import com.d104.domain.usecase.SendWebRTCUseCase
import com.d104.domain.utils.StompConnectionState
import com.d104.yogaapp.utils.ImageStorageManager
import com.d104.yogaapp.utils.bitmapToBase64
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.time.LocalDateTime
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
    private val getUserIdUseCase: GetUserIdUseCase,
    private val postYogaPoseHistoryUseCase: PostYogaPoseHistoryUseCase,
    private val imageStorageManager: ImageStorageManager
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

            // 타이머 종료 후
            if (uiState.value.timerProgress <= 0f && (uiState.value.currentRoom!!.userId.toString() == uiState.value.myId)) {
                sendRoundEndMessage()
            }
        }
    }

    fun processIntent(intent: MultiPlayIntent) {
        if (intent is MultiPlayIntent.ExitRoom || intent is MultiPlayIntent.Exit) {
            cancelPlayTimer()
            // cancelNextRoundTimer() // <- 제거됨
        }
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
                    processIntent(MultiPlayIntent.RoundEnded)
                }
                if (intent.message.type == "user_joined") {
                    Timber.d("User joined: ${intent.message}")
                    val peerId = (intent.message as UserJoinedMessage).fromPeerId
                    val nickname = intent.message.userNickName
                    if (!uiState.value.userList.keys.contains(peerId)) {
                        processIntent(
                            MultiPlayIntent.UserJoined(
                                PeerUser(
                                    id = peerId,
                                    nickName = nickname,
                                    isReady = false,
                                    totalScore = 0.0f,
                                    roundScore = 0.0f
                                )
                            )
                        )
                        sendJoinMessage()
                    } else {
                        Timber.d("User already joined: $peerId")
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
                        sendImageToServer()
                        processIntent(MultiPlayIntent.RoundStarted(state))
                    } else if (state == -1) {
                        Timber.d("Game ended")
                        processIntent(MultiPlayIntent.GameEnd)
                    }
                    startTimer()
                }
                if (intent.message.type == "user_left") {
                    processIntent(MultiPlayIntent.UserLeft(intent.message.fromPeerId))
                }
                if (intent.message.type == "user_ready") {
                    Timber.d("Received user_ready message for ID: ${intent.message.fromPeerId}")
                    // --- 게임 시작 조건 확인 (user_ready 시 확인) ---
                    checkAndSendStartMessageIfNeeded() // 게임 시작 조건 확인 로직 호출
                }
                if (intent.message.type == "request_photo") {
                    Timber.d("Received request_photo message")
                    sendImage()
                }
            }

            is MultiPlayIntent.SetCurrentHistory -> {

            }

            is MultiPlayIntent.ReadyClick -> {
                sendReadyMessage()
            }

            is MultiPlayIntent.ExitRoom -> {
                // 방 나가기 처리
                viewModelScope.launch {
                    val myId = getUserIdUseCase()
                    if (sendSignalingMessageUseCase(
                            myId,
                            uiState.value.currentRoom!!.roomId.toString(), 3
                        )
                    ) {
                        Timber.d("User left: $myId")
                        processIntent(MultiPlayIntent.Exit)
                    } else {
                        Timber.d("Failed to send user left message")
                    }
                }
            }

            is MultiPlayIntent.GameStarted -> {
                timerJob?.cancel()
                cancelPlayTimer()
                startTimer()
            }

            is MultiPlayIntent.RoundStarted -> {
                timerJob?.cancel()
                cancelPlayTimer()
                startTimer()
            }

            is MultiPlayIntent.ReceiveWebRTCImage -> {
                Timber.d("Received WebRTC image")
                sendImageToServer()
            }

            else -> {}
        }
    }

    private fun sendImageToServer() {
        viewModelScope.launch {
            val uri = imageStorageManager.saveImage(
                bitmap = uiState.value.bitmap!!,
                index = LocalDateTime.now().toString(),
                poseId = uiState.value.beyondPose.poseId.toString()
            )
            val sortedUserDatas = uiState.value.userList.values.sortedByDescending { it.totalScore }
            val currentUserId = uiState.value.myId
            // 정렬된 리스트에서 현재 사용자의 인덱스를 찾습니다.
            val rankingIndex = sortedUserDatas.indexOfFirst { it.id == currentUserId } // UserData 객체에 userId 속성이 있다고 가정
            postYogaPoseHistoryUseCase(
                poseId = uiState.value.beyondPose.poseId,
                roomRecordId = uiState.value.currentRoom!!.roomId,
                accuracy = uiState.value.accuracy,
                ranking = rankingIndex,
                poseTime = uiState.value.time,
                imgUri = uri.toString()
            )
        }
    }

    private fun cancelPlayTimer() {
        if (timerJob?.isActive == true) {
            Timber.d("Cancelling play timer.")
            timerJob?.cancel()
        }
        timerJob = null
    }

    override fun onCleared() {
        Timber.d("ViewModel cleared. Closing WebRTC and WebSocket.")
        // 여기서 확실하게 리소스 해제
        closeWebSocketUseCase() // UseCase 내부에서 이미 종료되었는지 확인 로직이 있다면 더 좋음
        closeWebRTCUseCase()  // UseCase 내부에서 이미 종료되었는지 확인 로직이 있다면 더 좋음
        super.onCleared()
    }

    private fun checkAndSendStartMessageIfNeeded() {
        val currentState = _uiState.value // 현재 상태 가져오기

        // currentRoom이 null이면 시작할 수 없음
        val room = currentState.currentRoom ?: run {
            Timber.w("checkAndSendStartMessageIfNeeded: currentRoom is null, cannot start game.")
            return
        }

        // 조건 1: 모든 유저가 준비 상태인가?
        val allUsersReady = currentState.userList.isNotEmpty() && // 유저 목록이 비어있지 않고
                currentState.userList.values.all { it.isReady } // 모든 유저의 isReady가 true

        // 조건 2: 현재 유저 수가 방 최대 인원과 같은가?
        val roomIsFull = currentState.userList.size == room.roomMax

        Timber.d("Checking start conditions: All Ready = $allUsersReady, Room Full = $roomIsFull (Current: ${currentState.userList.size}, Max: ${room.roomMax})")

        // 두 조건이 모두 참일 때만 시작 메시지 전송
        if (allUsersReady && roomIsFull) {
            Timber.i("All conditions met! Sending start game message.")
            sendStartMessage() // 게임 시작 메시지 전송 함수 호출
        }
    }

    private fun sendGameEndMessage() {
        viewModelScope.launch {
            val id = getUserIdUseCase()
            sendSignalingMessageUseCase(
                id,
                uiState.value.currentRoom!!.roomId.toString(),
                5,
                round = -1
            )
        }
    }

    private fun sendReadyMessage() {
        viewModelScope.launch {
            val id = getUserIdUseCase()
            uiState.value.userList[id]?.let {
                sendSignalingMessageUseCase(
                    id,
                    uiState.value.currentRoom!!.roomId.toString(),
                    if (it.isReady) 2 else 1
                )
            }
        }
    }

    private fun sendRoundEndMessage() {
        viewModelScope.launch {
            val myId = getUserIdUseCase()
            val currentRoom = _uiState.value.currentRoom ?: return@launch
            val currentRoomId = currentRoom.roomId.toString()

            // 호스트가 아니면 아무것도 안 함 (이 함수는 호스트만 호출해야 함)
            if (currentRoom.userId.toString() != myId) {
                Timber.w("sendRoundEndMessage called by non-host ($myId). Ignoring.")
                return@launch
            }

            // 1. 라운드 종료 메시지 전송 (호스트가 서버로 보냄 -> 서버가 브로드캐스트)
            Timber.i("Host ($myId) sending round_end message (type 6).")
            sendSignalingMessageUseCase(myId, currentRoomId, 6)

            // 2. 최고 점수 계산 및 사진 요청 (1초 딜레이 후)
            Timber.d("Host ($myId) waiting 1 second to request photo.")
            delay(1000L)
            val stateAfterPhotoDelay = _uiState.value
            val userListForPhoto = stateAfterPhotoDelay.userList
            if (userListForPhoto.isNotEmpty()) {
                val topScorerEntryPhoto = userListForPhoto.maxByOrNull { it.value.roundScore }
                if (topScorerEntryPhoto != null) {
                    Timber.i("Host requesting photo from top scorer: ${topScorerEntryPhoto.key}")
                    requestPhoto(topScorerEntryPhoto.key)
                } else {
                    Timber.w("Host could not determine top scorer for photo request.")
                }
            } else {
                Timber.w("User list empty, skipping photo request.")
            }

            // 3. 다음 라운드/게임 종료 결정을 위한 10초 대기 (사진 요청 후 시작)
            Timber.i("Host ($myId) waiting 10 seconds before next round/game end decision.")
            delay(10_000L)

            // 4. 10초 후 다음 액션 결정 및 서버 메시지 전송
            val stateAfter10s = _uiState.value // 10초 후 최신 상태 확인
            if (stateAfter10s.gameState != GameState.RoundResult) {
                // 10초 동안 상태가 바뀌었다면 (예: 누군가 나감) 액션 중단
                Timber.w("Host's 10s delay finished, but state is no longer RoundResult (${stateAfter10s.gameState}). Aborting.")
                return@launch
            }

            val nextRoundIndex = stateAfter10s.roundIndex + 1
            val poses = stateAfter10s.currentRoom?.userCourse?.poses

            if (poses != null && nextRoundIndex < poses.size) {
                // 다음 라운드 시작 요청
                Timber.i("Host sending next round message for round: $nextRoundIndex")
                sendNextRoundMessage(nextRoundIndex)
            } else {
                // 게임 종료 요청
                Timber.i("Host sending game end message.")
                sendGameEndMessage()
            }
        }
    }

    private fun sendNextRoundMessage(nextRoundIndex: Int) {
        viewModelScope.launch {
            val id = getUserIdUseCase()
            sendSignalingMessageUseCase(
                id,
                uiState.value.currentRoom!!.roomId.toString(),
                5,
                round = nextRoundIndex
            )
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
                    score = uiState.value.score,
                    time = uiState.value.second
                )
            )
        }
    }

    private fun requestPhoto(toPeerId: String) {
        viewModelScope.launch {
            val myId = getUserIdUseCase()
            sendSignalingMessageUseCase(
                myId,
                uiState.value.currentRoom!!.roomId.toString(),
                type = 5,
                toPeerId = toPeerId,
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
                val yogaPoses =
                    (uiState.map { it.currentRoom }).filterNotNull().first().userCourse.poses
                Timber.d("Yoga poses: $yogaPoses")
                viewModelScope.launch {
                    // myId 먼저 가져오기 (예시: .first() 사용 등으로 동기적으로 기다리거나)
                    // 혹은 완료 후 Intent 호출
                    val fetchedMyId = getUserIdUseCase() // 만약 동기적이지 않다면 아래처럼 launch 안에서 처리
                    _uiState.update { it.copy(myId = fetchedMyId) } // 또는 processIntent 사용
                    Timber.d("My ID set in ViewModel state: $fetchedMyId") // 로그 추가

                    // 이후 Room ID 가져오고 웹소켓 연결 등 진행
                    // ... (기존 웹소켓 로직) ...
                }
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
                connectWebSocketUseCase(roomId)
                    .catch { exception -> // <<<---- 여기에 .catch 연산자 추가!
                        // Flow 처리 중 발생하는 모든 예외(StompErrorException 포함)를 여기서 잡습니다.
                        Timber.e(exception, "Error caught during WebSocket message collection for room $roomId")

                        // 사용자에게 오류 알림 또는 상태 업데이트 (예시)
                        // MultiPlayIntent에 오류 처리를 위한 타입을 추가하고 사용하세요.
                        // 예: processIntent(MultiPlayIntent.WebSocketConnectionError(exception))
                        // 또는 간단히 상태 업데이트
                        processIntent(MultiPlayIntent.Exit) // 방 나가기 처리
                        // 필요하다면 여기서 연결 해제 로직 호출 또는 재연결 시도 로직 구현
                        // closeWebSocketUseCase() // 필요 시 명시적 해제
                    }.collect { msg ->
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