package com.d104.yogaapp.features.multi.play

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d104.domain.model.GameStateMessage
import com.d104.domain.model.IceCandidateMessage
import com.d104.domain.model.ImageChunkMessage
import com.d104.domain.model.PeerUser
import com.d104.domain.model.RequestPhotoMessage
import com.d104.domain.model.ScoreUpdateMessage
import com.d104.domain.model.TotalScoreMessage
import com.d104.domain.model.UserJoinedMessage
import com.d104.domain.usecase.CloseWebRTCUseCase
import com.d104.domain.usecase.CloseWebSocketUseCase
import com.d104.domain.usecase.ConnectWebSocketUseCase
import com.d104.domain.usecase.GetMultiAllPhotoUseCase
import com.d104.domain.usecase.GetMultiBestPhotoUseCase
import com.d104.domain.usecase.GetUserIdUseCase
import com.d104.domain.usecase.GetUserNickNameUseCase
import com.d104.domain.usecase.HandleSignalingMessage
import com.d104.domain.usecase.InitializeWebRTCUseCase
import com.d104.domain.usecase.InitiateConnectionUseCase
import com.d104.domain.usecase.ObserveChunkImageUseCase
import com.d104.domain.usecase.ObserveWebRTCMessageUseCase
import com.d104.domain.usecase.ObserveWebSocketConnectionStateUseCase
import com.d104.domain.usecase.PostYogaPoseHistoryUseCase
import com.d104.domain.usecase.ProcessChunkImageUseCase
import com.d104.domain.usecase.SendImageUseCase
import com.d104.domain.usecase.SendRoomRecordUseCase
import com.d104.domain.usecase.SendSignalingMessageUseCase
import com.d104.domain.usecase.SendWebRTCUseCase
import com.d104.domain.utils.StompConnectionState
import com.d104.yogaapp.R
import com.d104.yogaapp.features.solo.play.DownloadState
import com.d104.yogaapp.utils.ImageDownloader
import com.d104.yogaapp.utils.ImageStorageManager
import com.d104.yogaapp.utils.bitmapToBase64
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
    @ApplicationContext private val context: Context,
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
    private val getUserNameUseCase: GetUserNickNameUseCase,
    private val postYogaPoseHistoryUseCase: PostYogaPoseHistoryUseCase,
    private val imageStorageManager: ImageStorageManager,
    private val getBestPoseRecordsUseCase: GetMultiBestPhotoUseCase,
    private val getMultiAllPhotoUseCase: GetMultiAllPhotoUseCase,
    private val imageDownloader: ImageDownloader,
    private val sendRoomRecordUseCase: SendRoomRecordUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MultiPlayState())
    val uiState: StateFlow<MultiPlayState> = _uiState.asStateFlow()
    private var currentTimerStep: Float = 1f
    private val totalTimeMs = 20_000L
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
            if (uiState.value.gameState != GameState.GameResult && uiState.value.timerProgress <= 0f && (uiState.value.currentRoom!!.userId.toString() == uiState.value.myId)) {

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
                Timber.d("Received WebSocket message: ${intent.message}")
                if (intent.message.type == "total_score") {
                    Timber.d("Received total_score message: ${intent.message}")
                    val totalScoreMessage = intent.message as TotalScoreMessage
                    val peerId = totalScoreMessage.toPeerId
                    val score = totalScoreMessage.score
                    processIntent(MultiPlayIntent.UpdateTotalScore(peerId, score))
                }
                if (intent.message.type == "round_end") {

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
                                    totalScore = 0,
                                    roundScore = 0.0f,
                                    iconUrl = intent.message.userIcon
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
                        startTimer()
                    } else if (state >= 1) {
                        Timber.d("Round $state started")
                        processIntent(MultiPlayIntent.RoundStarted(state))
                        startTimer()
                    } else if (state == -1) {
                        Timber.d("Game ended")
                        processIntent(MultiPlayIntent.GameEnd)
                        viewModelScope.launch {
                            getBestPoseRecordsUseCase(uiState.value.currentRoom!!.roomId).collect { it ->
                                it.onSuccess {
                                    Timber.d("Best pose records: $it")
                                    processIntent(MultiPlayIntent.BestPose(it))
                                }.onFailure {
                                    Timber.e("Failed to get best pose records: $it")
                                }
                            }
                        }
                        sendGameResult()
                    }
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
                    if ((intent.message as RequestPhotoMessage).toPeerId == uiState.value.myId) {
                        sendImageToMeshNetwork()
                    }
                    sendImageToServer()
                }
            }

            is MultiPlayIntent.ReadyClick -> {
                sendReadyMessage()
            }

            is MultiPlayIntent.ExitRoom -> {
                // 방 나가기 처리
                sendLeftMessage()
            }

            is MultiPlayIntent.GameStarted -> {
                timerJob?.cancel()
                cancelPlayTimer()
                startTimer()
            }

            is MultiPlayIntent.RoundStarted -> {
                Timber.d("Round started: ${intent.state}")
                timerJob?.cancel()
                cancelPlayTimer()
                startTimer()
            }

            is MultiPlayIntent.SendHistory -> {
                Timber.d("Send history")
                sendScore()
            }

            is MultiPlayIntent.ClickPhoto -> {
                getPhotos(intent.it)
            }

            else -> {}
        }
    }

    private fun sendGameResult() {
        viewModelScope.launch {
            val myId = getUserIdUseCase()
            val userList = uiState.value.userList
            val sortedUsers = userList.values.sortedWith(
                compareByDescending<PeerUser> { it.totalScore }
                    .thenBy { it.id }
            )
            val myRank = sortedUsers.indexOfFirst { it.id == myId } + 1
            sendRoomRecordUseCase(
                roomId = uiState.value.currentRoom!!.roomId.toString(),
                totalRanking = myRank,
                totalScore = userList[myId]?.totalScore?: 0
            ).collect() { result ->
                result.onSuccess {
                    Timber.d("Room record sent successfully")
                }.onFailure {
                    Timber.e("Failed to send room record: $it")
                }
            }
        }
    }

    private fun getPhotos(it: Int) {
        Timber.d("Getting photos for pose ID: $it")
        viewModelScope.launch {
            getMultiAllPhotoUseCase(uiState.value.currentRoom!!.roomId, it).collect { result ->
                result.onSuccess {
                    Timber.d("Received photos: $it")
                    processIntent(MultiPlayIntent.AllPose(it))
                }.onFailure {
                    Timber.e("Failed to get photos: $it")
                }
            }
        }
    }

    private fun sendLeftMessage() {
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

    private fun sendImageToServer() {
        val logTag = "MultiPlay_ImageToServer:" // 로그 필터링을 위한 태그

        viewModelScope.launch {
            Timber.d("$logTag Starting image saving process.")

            try {
                val currentState = uiState.value
                Timber.d("$logTag Current state captured. My ID: ${currentState.myId}, Room ID: ${currentState.currentRoom?.roomId}, Bitmap available: ${currentState.bitmap != null}")

                // --- 1. 사용할 비트맵 결정 ---
                val bitmapSource: String
                val bitmapToUse: Bitmap? = if (currentState.bitmap != null) {
                    bitmapSource = "UI State"
                    Timber.d("$logTag Using bitmap from UI state.")
                    currentState.bitmap
                } else {
                    bitmapSource = "Default Drawable"
                    Timber.w("$logTag UI state bitmap is null. Attempting to load default image from drawable.")
                    withContext(Dispatchers.IO) {
                        Timber.d("$logTag Loading default bitmap from resource ID: ${R.drawable.ic_launcher_foreground}")
                        loadBitmapFromDrawable(context, R.drawable.ic_launcher_foreground)
                    }
                }
                Timber.d("$logTag Bitmap source selected: $bitmapSource. Bitmap acquired: ${bitmapToUse != null}")

                // --- 2. 비트맵 확보 실패 시 처리 ---
                if (bitmapToUse == null) {
                    Timber.e("$logTag Failed to get bitmap (source: $bitmapSource). Aborting image save.")
                    return@launch
                }
                Timber.d("$logTag Bitmap acquired successfully. Dimensions: ${bitmapToUse.width}x${bitmapToUse.height}, Config: ${bitmapToUse.config}")

                // --- 3. 결정된 비트맵으로 이미지 저장 ---
                val poseIdStr = currentState.currentPose.poseId.toString()
                val indexStr = LocalDateTime.now().toString()
                Timber.d("$logTag Saving bitmap to storage via ImageStorageManager. Pose ID: $poseIdStr, Index: $indexStr")

                val uri = try {
                    imageStorageManager.saveImage(
                        bitmap = bitmapToUse,
                        index = indexStr,
                        poseId = poseIdStr
                    )
                } catch (e: Exception) {
                    Timber.e(e, "$logTag Exception occurred during image saving.")
                    return@launch
                }

                if (uri == null) {
                    Timber.e("$logTag ImageStorageManager returned null URI. Aborting image save.")
                    return@launch
                }
                Timber.d("$logTag Bitmap saved successfully. URI: $uri")

                // --- 4. 랭킹 계산 ---
                val currentUserId = currentState.myId
                Timber.d("$logTag Calculating ranking for user: $currentUserId")
                val ranking = if (currentState.userList.isNotEmpty()) {
                    // --- 기존 sortedWith/indexOfFirst 로직 대체 시작 ---
                    val userListValues = currentState.userList.values // 사용자 목록 값 가져오기
                    val currentUserData = currentState.userList[currentUserId] // 현재 사용자 데이터 가져오기

                    if (currentUserData == null) {
                        // 현재 사용자를 목록에서 찾을 수 없는 경우 (이론상 발생하면 안 됨)
                        Timber.w("$logTag Current user $currentUserId not found in userList. Cannot calculate rank.")
                        -1
                    } else {
                        val currentUserScore = currentUserData.roundScore
                        val currentUserIdValue = currentUserData.id // 비교용 ID 저장

                        // 현재 사용자 점수 유효성 검사 (NaN/Infinity)
                        if (currentUserScore.isNaN() || currentUserScore.isInfinite()) {
                            Timber.w("$logTag Current user $currentUserId has invalid score ($currentUserScore). Rank is -1.")
                            -1
                        } else {
                            var higherRankCount = 0 // 자신보다 순위가 높은 사용자 수
                            // 다른 모든 사용자와 비교
                            for (otherUser in userListValues) {
                                // 자기 자신은 건너뛰기
                                if (otherUser.id == currentUserIdValue) continue

                                val otherUserScore = otherUser.roundScore
                                val otherUserId = otherUser.id

                                // 다른 사용자 점수 유효성 검사 (NaN/Infinity) - 유효하지 않으면 비교에서 제외
                                if (otherUserScore.isNaN() || otherUserScore.isInfinite()) continue

                                // 다른 사용자가 더 높은 순위인지 확인
                                if (otherUserScore > currentUserScore || (otherUserScore == currentUserScore && otherUserId < currentUserIdValue)) {
                                    higherRankCount++
                                }
                            }
                            // 최종 랭크 반환 (자신보다 높은 순위 수 + 1)
                            higherRankCount + 1
                        }
                    }
                    // --- 기존 sortedWith/indexOfFirst 로직 대체 끝 ---
                } else {
                    // userList가 비어있는 경우는 그대로 유지
                    Timber.w("$logTag User list is empty, cannot calculate ranking. Setting rank to -1.")
                    -1
                }
                Timber.d("$logTag Ranking calculated. Rank: $ranking")

                if (ranking == -1 && currentState.userList.isNotEmpty()) {
                    Timber.w("$logTag Could not determine ranking for user $currentUserId among ${currentState.userList.size} users.")
                }

                // --- 5. 결과 전송 (API 호출) ---
                val poseId = currentState.currentPose.poseId
                val roomId = currentState.currentRoom?.roomId
                if (roomId == null) {
                    Timber.e("$logTag Cannot post history, currentRoom or roomId is null.")
                    return@launch
                }
                val accuracy = currentState.accuracy
                val poseTime = currentState.time
                val imageUriString = uri.toString()

                Timber.d("$logTag Preparing to post yoga pose history. Pose ID: $poseId, Room ID: $roomId, Accuracy: $accuracy, Rank: $ranking, Time: $poseTime, URI: $imageUriString")

                // UseCase 호출 및 결과 처리 (Flow<Result<YogaPoseRecord>> 가정)
                postYogaPoseHistoryUseCase(
                    poseId = poseId,
                    roomRecordId = roomId,
                    accuracy = accuracy,
                    ranking = ranking,
                    poseTime = poseTime,
                    imgUri = imageUriString
                ).collect { result -> // Flow를 collect하여 결과 처리
                    result.onSuccess { yogaPoseRecord ->
                        // 성공 시 로그: 반환된 데이터 포함
                        Timber.i("$logTag Yoga pose history posted successfully. Received record: $yogaPoseRecord")
                        // 필요하다면 성공 후 추가 작업 (예: UI 상태 업데이트)
                    }.onFailure { exception ->
                        // 실패 시 로그: 예외 정보 포함
                        Timber.e(exception, "$logTag Failed to post yoga pose history.")
                        // 필요하다면 실패 처리 (예: 사용자에게 오류 메시지 표시)
                    }
                }
                // collect 블록 이후는 Flow가 완료된 후 실행됩니다.
                // 만약 Flow가 단일 값만 방출하고 완료된다면 이 위치에 도달합니다.
                // 무한 Flow라면 이 위치에 도달하지 않을 수 있습니다.
                Timber.d("$logTag Finished collecting results from postYogaPoseHistoryUseCase.")


            } catch (e: Exception) {
                Timber.e(e, "$logTag Uncaught exception during image saving process.")
            } finally {
                // finally 블록은 성공/실패/취소 여부와 관계없이 실행됨 (선택적)
                Timber.d("$logTag Image saving process execution block finished.")
            }
        }
    }

    private fun loadBitmapFromDrawable(context: Context, resourceId: Int): Bitmap? {
        return try {
            BitmapFactory.decodeResource(context.resources, resourceId)
        } catch (e: Exception) { // 리소스를 찾지 못하거나 디코딩 오류 등
            Timber.e(e, "Error loading bitmap from drawable resource: $resourceId")
            null // 실패 시 null 반환
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

    private fun sendMyReadyState() {
        viewModelScope.launch {
            val id = getUserIdUseCase()
            uiState.value.userList[id]?.let {
                sendSignalingMessageUseCase(
                    id,
                    uiState.value.currentRoom!!.roomId.toString(),
                    if (it.isReady) 1 else 2
                )
            }
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
                var manualTopScorer: Map.Entry<String, PeerUser>? = null
                for (entry in userListForPhoto.entries) {
                    val currentScore = entry.value.roundScore
                    // NaN이나 무한대 값은 일단 건너뛰도록 처리 (선택 사항)
                    if (currentScore.isNaN() || currentScore.isInfinite()) {
                        Timber.d("점수 Skipping user ${entry.value.nickName} due to special float value: $currentScore")
                        continue
                    }

                    if (manualTopScorer == null) {
                        manualTopScorer = entry
                    } else {
                        val topScore = manualTopScorer.value.roundScore
                        if (currentScore > topScore) {
                            manualTopScorer = entry
                        }
                        // 동점자 처리 로직 필요 시 추가 (예: ID 비교)
                         else if (currentScore == topScore && entry.value.id < manualTopScorer.value.id) {
                             manualTopScorer = entry
                         }
                    }
                }
// 결과 출력
                manualTopScorer?.let {
                    Timber.d("최고 점수 유저: ID=${it.value.nickName}, 점수=${it.value.roundScore}")
                } ?: Timber.d("유저가 없습니다.")
                if (manualTopScorer != null) {
                    Timber.i("Host requesting photo from top scorer: ${manualTopScorer.value.nickName}")
                    requestPhoto(manualTopScorer.key)
                } else {
                    Timber.w("Host could not determine top scorer for photo request.")
                }
                val descendingScores = userListForPhoto.entries.sortedWith(
                    compareByDescending<Map.Entry<String, PeerUser>> { it.value.roundScore }
                        .thenBy { it.value.id }
                )
                descendingScores.forEachIndexed() { index, it ->
                    // total score 업데이트 명령
                    sendTotalScoreMessage(it.value.id, 10 - index * 3)
                    Timber.i("User: ${it.key}, Score: ${it.value.roundScore}")
                }
            } else {
                Timber.w("User list empty, skipping photo request.")
            }

            // 3. 다음 라운드/게임 종료 결정을 위한 5초 대기 (사진 요청 후 시작)
            Timber.i("Host ($myId) waiting 10 seconds before next round/game end decision.")
            delay(10_000L)

            val stateAfter10s = _uiState.value // 10초 후 최신 상태 확인
            if (stateAfter10s.gameState != GameState.RoundResult) {
                // 10초 동안 상태가 바뀌었다면 (예: 누군가 나감) 액션 중단
                Timber.w("Host's 10s delay finished, but state is no longer RoundResult (${stateAfter10s.gameState}). Aborting.")
                return@launch
            }
            Timber.i("Round!!!", stateAfter10s.roundIndex)
            // 로직 점검
            val nextRoundIndex = stateAfter10s.roundIndex + 1
            val poses = stateAfter10s.currentRoom?.userCourse?.poses
            if (poses != null && nextRoundIndex < poses.size) {
                // 다음 라운드 시작 요청
                Timber.i("Host sending next round message for round: $nextRoundIndex")
                Timber.i("$nextRoundIndex, ${poses.size}")
                sendNextRoundMessage(nextRoundIndex)
            } else {
                // 게임 종료 요청
                Timber.i("Host sending game end message.")
                Timber.i("$nextRoundIndex, ${poses ?: "null"}")
                sendGameEndMessage()
            }
        }
    }

    private fun sendTotalScoreMessage(toPeerId: String, score: Int) {
        viewModelScope.launch {
            val id = getUserIdUseCase()
            sendSignalingMessageUseCase(
                fromPeerId = id,
                destination = uiState.value.currentRoom!!.roomId.toString(),
                8,
                toPeerId = toPeerId,
                score = score
            )
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
                0,
            )
        }
        sendMyReadyState()
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

    private fun sendImageToMeshNetwork() {
        processIntent(MultiPlayIntent.SetBestImage(uiState.value.bitmap!!))
        viewModelScope.launch {
            val currentState = uiState.value

            // 1. 사용할 비트맵 결정: 상태에 있으면 사용, 없으면 drawable에서 로드
            val bitmapToUse: Bitmap? = if (currentState.bitmap != null) {
                Timber.d("Using bitmap from UI state for WebRTC send.")
                currentState.bitmap
            } else {
                Timber.w("UI state bitmap is null. Attempting to load default image from drawable for WebRTC send.")
                // Drawable 리소스 로드 시도 (IO 작업이므로 withContext 사용 권장)
                withContext(Dispatchers.IO) {
                    // sendImageToServer와 동일한 기본 이미지를 사용하거나 다른 이미지를 지정할 수 있습니다.
                    loadBitmapFromDrawable(
                        context,
                        R.drawable.ic_launcher_foreground
                    ) // <<<--- 기본 이미지 리소스 ID 지정
                }
            }

            // 2. 비트맵 확보 실패 시 처리
            if (bitmapToUse == null) {
                Timber.e("Failed to get bitmap (neither from state nor drawable). Cannot send image via WebRTC.")
                // 오류 처리: 사용자에게 알림, 로그만 남기기 등
                return@launch // 코루틴 실행 중단
            }

            // 3. 결정된 비트맵을 Base64로 인코딩
            // bitmapToBase64 함수가 null을 반환할 수 있는지 확인 필요
            // 만약 null 반환 가능하다면 추가 처리 필요
            val imageBytesBase64: ByteArray? =
                withContext(Dispatchers.Default) { // 인코딩은 CPU 작업이므로 Default 디스패처 사용 가능
                    bitmapToBase64(bitmapToUse) // bitmapToUse는 null이 아님
                }

            // 4. Base64 인코딩 실패 시 처리
            if (imageBytesBase64 == null) {
                Timber.e("Failed to encode bitmap to Base64. Cannot send image via WebRTC.")
                // 오류 처리
                return@launch
            }

            // *** 추가된 로그: Base64 인코딩된 데이터의 크기 확인 ***
            Timber.d("sendImageToMeshNetwork: imageBytesBase64 size = ${imageBytesBase64.size}")

            // 5. WebRTC를 통해 이미지 전송
            Timber.d("Sending image via WebRTC...") // 이 로그는 이미 있음
            sendImageUseCase(
                params = SendImageUseCase.Params(
                    imageBytes = imageBytesBase64, // null 이 아님이 보장됨
                    targetPeerId = null,
                    quality = 85
                )
            )
        }
    }

    private fun sendScore() {
        processIntent(
            MultiPlayIntent.UpdateScore(
                uiState.value.myId!!,
                ScoreUpdateMessage(
                    score = uiState.value.time,
                    time = uiState.value.time
                )
            )
        )
        viewModelScope.launch {
            sendWebRTCUseCase(
                message = ScoreUpdateMessage(
                    score = uiState.value.accuracy,
                    time = uiState.value.time
                )
            )
        }

        Timber.d("Sending score via WebRTC...${uiState.value.time}")
    }

    private fun requestPhoto(toPeerId: String) {
        viewModelScope.launch {
            val myId = getUserIdUseCase()
            sendSignalingMessageUseCase(
                fromPeerId = myId,
                uiState.value.currentRoom!!.roomId.toString(),
                type = 7,
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

    fun save(uri: Uri, fileName: String) {
        viewModelScope.launch {
            try {
                val success = imageDownloader.saveImageToGallery(uri.toString(), fileName)
                if (success) {
                    // 성공 시 Toast 메시지 표시
                    Toast.makeText(context, "이미지가 갤러리에 저장되었습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    // 실패 시 Toast 메시지 표시
                    Toast.makeText(context, "이미지 저장에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                // 예외 발생 시 Toast 메시지 표시
                Toast.makeText(context, "오류 발생: ${e.message}", Toast.LENGTH_SHORT).show()
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
                Timber.d("Yoga poses: ${yogaPoses.size}")
                viewModelScope.launch {
                    // myId 먼저 가져오기 (예시: .first() 사용 등으로 동기적으로 기다리거나)
                    // 혹은 완료 후 Intent 호출
                    val fetchedMyId = getUserIdUseCase() // 만약 동기적이지 않다면 아래처럼 launch 안에서 처리
                    _uiState.update { it.copy(myId = fetchedMyId) } // 또는 processIntent 사용
                    Timber.d("My ID set in ViewModel state: $fetchedMyId") // 로그 추가
                    val myName = getUserNameUseCase()
                    _uiState.update { it.copy(myName = myName) }
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
                        Timber.e(
                            exception,
                            "Error caught during WebSocket message collection for room $roomId"
                        )

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
                            Timber.d("Received ImageChunkMessage: ${it.second}")
                            processChunkImageUseCase((it.second as ImageChunkMessage))
                        }
                        is ScoreUpdateMessage -> {
                            Timber.d("Received ScoreUpdateMessage: ${it.second}")
                            processIntent(
                                MultiPlayIntent.UpdateScore(
                                    it.first,
                                    (it.second as ScoreUpdateMessage)
                                )
                            )
                        }
                    }
                }
                Timber.d("Received WebRTC message: ${it.second}")
            }
        }

        viewModelScope.launch {
            Timber.d("Attempting to collect from observeChunkImageUseCase...") // Flow 구독 시작 로그
            observeChunkImageUseCase().collect { byteArray -> // Flow<ByteArray> 구독
                Timber.d("Collected completed image ByteArray from observeChunkImageUseCase! Size: ${byteArray.size}")

                // IO 디스패처에서 비트맵 변환 수행
                val bitmap: Bitmap? = withContext(Dispatchers.IO) {
                    try {
                        // BitmapFactory를 사용하여 ByteArray를 Bitmap으로 디코딩
                        BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                    } catch (e: Exception) {
                        // 디코딩 중 에러 발생 시 (예: 메모리 부족, 잘못된 바이트 배열 등)
                        Timber.e(e, "Failed to decode ByteArray to Bitmap.")
                        null // 실패 시 null 반환
                    } catch (oom: OutOfMemoryError) {
                        // 메모리 부족 에러는 별도 처리
                        Timber.e(oom, "OutOfMemoryError while decoding ByteArray to Bitmap.")
                        // TODO: 메모리 부족 상황에 대한 처리 (예: 사용자 알림, 리소스 정리 등)
                        null // 실패 시 null 반환
                    }
                }

                // 비트맵 변환 결과 처리
                if (bitmap != null) {
                    // 성공적으로 Bitmap 생성됨
                    Timber.d("Successfully decoded ByteArray to Bitmap. Sending ReceiveWebRTCImage intent.")
                    // 생성된 Bitmap으로 Intent 전송
                    processIntent(MultiPlayIntent.ReceiveWebRTCImage(bitmap))
                } else {
                    // Bitmap 생성 실패
                    Timber.w("Bitmap decoding resulted in null. Skipping ReceiveWebRTCImage intent.")
                    // 필요하다면 에러 상태 업데이트 또는 사용자 알림
                    // processIntent(MultiPlayIntent.ShowError("이미지 로딩 실패"))
                }
            }
        }
    }
}