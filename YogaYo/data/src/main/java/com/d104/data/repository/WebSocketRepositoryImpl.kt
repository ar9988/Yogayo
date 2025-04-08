package com.d104.data.repository

import android.util.Log
import com.d104.data.local.dao.PreferencesDao
import com.d104.data.remote.api.WebSocketService
import com.d104.data.remote.utils.StompUtils
import com.d104.domain.model.StompErrorException
import com.d104.domain.model.UserJoinedMessage
import com.d104.domain.utils.StompConnectionState
import com.d104.domain.repository.WebSocketRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebSocketRepositoryImpl @Inject constructor(
    private val webSocketService: WebSocketService,
    private val datastoreDao: PreferencesDao,
) : WebSocketRepository {

    private val webSocketUrl = "wss://j12d104.p.ssafy.io/ws" // 엔드포인트 확인
    private val host = "j12d104.p.ssafy.io" // CONNECT 프레임 호스트 확인

    // --- 상태 관리 ---
    private val _connectionState = MutableStateFlow(StompConnectionState.DISCONNECTED)
    override val connectionState: StateFlow<StompConnectionState> = _connectionState.asStateFlow()

    // 현재 연결된 방 ID 및 토픽 정보
    private var currentRoomId: String? = null
    private var currentTopic: String? = null
    private var currentSubscriptionId: String? = null

    // 메시지 Flow (단일 방 구독용)
    private var messageFlow: SharedFlow<String>? = null // 공유 Flow
    private var messageFlowJob: Job? = null // Flow 생성/관찰 작업을 관리할 Job
    private val repositoryScope = CoroutineScope(Job() + Dispatchers.IO)

    private val webSocketListener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            // 이 onOpen은 connect 로직에서 직접 호출되지 않습니다.
            Log.w("StompRepo", "External webSocketListener onOpen called - Unexpected in connect flow.")
        }
        override fun onMessage(webSocket: WebSocket, text: String) { /* No-op */ }
        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) { handleDisconnect("WebSocket Closing (External Listener)") }
        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) { handleDisconnect("WebSocket Closed (External Listener)") }
        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) { handleConnectionFailure(t) }
    }

    override suspend fun connect(topic: String): Flow<String> {
        Log.d("StompRepo", "connect() called for topic: $topic") // 함수 호출 확인

        // 0. 이미 연결 상태 확인 (기존 코드 유지)
        if (currentRoomId == topic && _connectionState.value == StompConnectionState.CONNECTED && messageFlow != null) {
            Log.d("StompRepo", "Already connected to room $topic. Returning existing flow.")
            return messageFlow!!
        }

        // 1. 다른 방 연결 해제 (기존 코드 유지)
        if (_connectionState.value != StompConnectionState.DISCONNECTED && currentRoomId != topic) {
            Log.w("StompRepo", "Switching rooms. Disconnecting from $currentRoomId first.")
            disconnect()
            _connectionState.first { it == StompConnectionState.DISCONNECTED }
        }

        // 2. 상태 초기화 (기존 코드 유지)
        _connectionState.value = StompConnectionState.CONNECTING
        currentRoomId = topic
        currentTopic = "/topic/room/$topic"
        currentSubscriptionId = "sub-$topic-${UUID.randomUUID().toString().take(8)}"
        messageFlowJob?.cancel()

        Log.d("StompRepo", "Starting channelFlow block...") // channelFlow 시작 확인

        // 3. channelFlow 생성 및 내부 리스너 정의 (핵심 수정 영역)
        val internalFlow = channelFlow<String> {
            // channelFlow 내부에서 사용할 WebSocket 리스너 정의
            val forwardingListener = object : WebSocketListener() {

                // ===== 핵심 수정 메서드: forwardingListener.onOpen =====
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    Log.d("StompRepo", ">>> forwardingListener onOpen CALLED! Preparing to send CONNECT...")

                    // CoroutineScope 내에서 suspend 함수 호출 및 CONNECT 프레임 전송
                    launch { // channelFlow의 CoroutineScope 사용
                        Log.d("StompRepo", ">>> launch block entered in onOpen")
                        var token: String? = null // 토큰 변수 초기화
                        try {
                            Log.d("StompRepo", ">>> PRE: Attempting dataStorePreferencesDao.getAccessToken().first()")
                            token = datastoreDao.getAccessToken().first() // 실제 토큰 가져오기 (suspend 함수)
                            Log.d("StompRepo", ">>> POST: Token retrieved: ${token != null}")

                            if (token != null) {
                                Log.d("StompRepo", ">>> Building CONNECT frame with token...")
                                val connectFrame = StompUtils.buildConnectFrame(host, token) // 실제 토큰 사용

                                Log.d("StompRepo", ">>> Sending CONNECT frame via webSocketService...")
                                val sent = webSocketService.send(connectFrame)
                                Log.d("StompRepo", ">>> webSocketService.send(CONNECT) result: $sent")

                                if (sent) {
                                    Log.i("StompRepo", ">>> STOMP CONNECT frame sent successfully.")
                                } else {
                                    Log.e("StompRepo", ">>> Failed to send STOMP CONNECT frame via webSocketService.")
                                    handleConnectionFailure(Exception("Failed to send CONNECT frame"))
                                    close(Exception("Failed to send CONNECT frame")) // Flow 종료
                                }
                            } else {
                                Log.e("StompRepo", ">>> Access Token is NULL. Cannot send CONNECT frame.")
                                handleConnectionFailure(Exception("Access token not available"))
                                close(Exception("Access token not available")) // Flow 종료
                            }
                        } catch (e: Exception) {
                            Log.e("StompRepo", "!!! Exception in onOpen launch block (getAccessToken or send) !!!", e)
                            handleConnectionFailure(e) // 연결 실패 처리
                            close(e) // Flow 종료
                        }
                    } // launch 끝
                } // onOpen 끝

                // ===== 이하 리스너 메서드들은 기존 로직 유지 =====
                override fun onMessage(webSocket: WebSocket, text: String) {
                    Log.d("StompRepo", ">>> forwardingListener onMessage received raw: ${text.take(200)}...")
                    try {
                        val (command, headers, body) = StompUtils.parseFrame(text)
                        Log.d("StompRepo", "Processing Frame: Command=$command, SubId=${headers["subscription"]}")

                        when (command) {
                            "CONNECTED" -> {
                                Log.i("StompRepo", "STOMP CONNECTED frame received.")
                                _connectionState.value = StompConnectionState.CONNECTED
                                sendSubscriptionFrame(currentTopic!!, currentSubscriptionId!!)
                            }
                            "MESSAGE" -> {
                                if (headers["subscription"] == currentSubscriptionId) {
                                    Log.d("StompRepo", "Message for current subscription received.")
                                    trySend(body) // Flow로 메시지 방출
                                } else {
                                    Log.w("StompRepo", "Received message for unrelated subscription: ${headers["subscription"]}")
                                }
                            }
                            "ERROR" -> {
                                Log.e("StompRepo", "STOMP ERROR frame received: $headers - $body")
                                try {
                                    close(StompErrorException("STOMP Error: ${headers["message"]} - $body"))
                                }catch (e: Exception) {
                                    Log.e("StompRepo", "Error closing Flow in ERROR frame", e)
                                }
                            }
                            // TODO: HEARTBEAT 프레임 처리 (필요시) -> 서버가 HEARTBEAT 보내는지 확인 필요
                            // "HEARTBEAT" -> Log.d("StompRepo", "Received HEARTBEAT frame from server.")
                            else -> {
                                Log.d("StompRepo", "ELSE frame received: $headers - $body")
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("StompRepo", "Error parsing STOMP frame in onMessage", e)
                        close(e)
                    }
                }

                override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                    Log.w("StompRepo", ">>> forwardingListener onClosing: Code=$code, Reason=$reason")
                    handleDisconnect("WebSocket Closing in Flow")
                    close() // Flow 종료
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    Log.w("StompRepo", ">>> forwardingListener onClosed: Code=$code, Reason=$reason")
                    handleDisconnect("WebSocket Closed in Flow")
                    close() // Flow 종료
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    Log.e("StompRepo", ">>> forwardingListener onFailure", t)
                    handleConnectionFailure(t)
                    close(t) // Flow를 에러와 함께 종료
                }
            } // forwardingListener 정의 끝

            // WebSocket 연결 시작 시 forwardingListener 전달
            Log.d("StompRepo", ">>> Calling webSocketService.connect() with forwardingListener...")
            webSocketService.connect(webSocketUrl, forwardingListener)

            // Flow 종료 대기
            awaitClose {
                Log.d("StompRepo", "ChannelFlow for room $topic closing.")
                // handleDisconnect는 각 콜백(onClosing, onClosed) 또는 외부 disconnect() 호출 시 처리됨
            }
        } // channelFlow 끝

        // 4. Flow 공유 및 관찰 (기존 코드 유지)
        messageFlow = internalFlow.shareIn(repositoryScope, SharingStarted.WhileSubscribed())
        messageFlowJob = repositoryScope.launch {
            messageFlow?.catch { e ->
                Log.e("StompRepo", "Error caught in shared message flow collector", e)
                // channelFlow 내부에서 에러 발생 시 여기서 잡힐 수 있음
                handleConnectionFailure(e)
            }?.collect{
                // ShareIn 사용 시 collect는 Flow 활성화를 위해 필요
                // 메시지 처리는 구독하는 곳(ViewModel 등)에서 수행
                Log.d("StompRepo", "Message collected in shared flow activator: ${it.take(50)}...")
            }
        }

        // 5. 연결 완료 대기 또는 타임아웃 (기존 코드 유지 - 필요에 따라 조정)
        try {
            Log.d("StompRepo", "Waiting for STOMP connection...")
            withTimeoutOrNull(15000) { // 타임아웃 15초
                _connectionState.first { it == StompConnectionState.CONNECTED }
            }
            if (_connectionState.value == StompConnectionState.CONNECTED) {
                Log.i("StompRepo", "STOMP connection established successfully.")
            } else {
                Log.e("StompRepo", "STOMP connection timed out after 15 seconds.")
                throw Exception("STOMP connection timed out for room $topic")
            }
        } catch (e: Exception) {
            Log.e("StompRepo", "Error during connection wait/timeout", e)
            handleConnectionFailure(e) // 이미 연결 실패 처리되었을 수 있음
            throw e // 실패를 호출자에게 전파
        }

        Log.i("StompRepo", "connect() method finished for topic $topic. Returning message flow.")
        return messageFlow!! // 위에서 할당 및 성공 확인 완료
    }

    override fun send(destination: String, message: String): Boolean {
        if (_connectionState.value != StompConnectionState.CONNECTED) {
            Log.w("StompRepo", "Cannot send message, STOMP not connected.")
            return false
        }
        // '/app/...' 형태의 destination 확인 필요
        Log.d("StompRepo", "Sending message : $message")
        val sendFrame = StompUtils.buildSendFrame("/app/room/$destination", message)
        val success = webSocketService.send(sendFrame)
        if (!success) {
            Log.e("StompRepo", "Failed to send message frame to $destination")
            // 전송 실패 처리 (예: 재시도 로직, 에러 알림)
        }
        return success
    }

    override fun getCurrentRoomId(): String? {
        return currentRoomId
    }

    override fun disconnect() {
        Log.d("StompRepo", "Disconnect requested by client.")
        handleDisconnect("Client request")
    }

    // --- 내부 헬퍼 함수 ---

    private fun sendSubscriptionFrame(topic: String, subId: String) {
        val subscribeFrame = StompUtils.buildSubscribeFrame(topic, subId)
        val sent = webSocketService.send(subscribeFrame)
        if (sent) {
            Log.i("StompRepo", "Sent SUBSCRIBE frame for topic: $topic (ID: $subId)")
        } else {
            Log.e("StompRepo", "Failed to send SUBSCRIBE frame for topic: $topic")
            // 구독 실패 처리 (예: 상태 변경, 재시도)
            handleConnectionFailure(Exception("Failed to send SUBSCRIBE frame"))
        }
    }

    // 연결 실패 처리 공통 로직
    private fun handleConnectionFailure(error: Throwable) {
        Log.e("StompRepo", "Connection Failure Handler: ${error.message}")
        if (_connectionState.value != StompConnectionState.DISCONNECTED) {
            webSocketService.disconnect() // WebSocket 즉시 닫기 시도
            _connectionState.value = StompConnectionState.ERROR // 에러 상태 표시
            // 잠시 후 DISCONNECTED로 변경하여 재연결 가능하게 할 수 있음
            repositoryScope.launch {
                delay(1000) // 잠시 대기 후
                if (_connectionState.value == StompConnectionState.ERROR) { // 다른 상태 변경 없었다면
                    _connectionState.value = StompConnectionState.DISCONNECTED
                }
            }
        }
        messageFlowJob?.cancel() // 현재 진행중인 Flow 작업 취소
        messageFlow = null
        currentRoomId = null
        currentTopic = null
        currentSubscriptionId = null
    }

    // 연결 해제 처리 공통 로직
    private fun handleDisconnect(reason: String) {
        if (_connectionState.value == StompConnectionState.DISCONNECTED) return // 이미 해제됨

        Log.i("StompRepo", "Handling disconnect. Reason: $reason")

        if (_connectionState.value == StompConnectionState.CONNECTED) {
            // DISCONNECT 프레임 전송 (정상 종료 시도)
            val disconnectFrame = StompUtils.buildDisconnectFrame()
            webSocketService.send(disconnectFrame)
            Log.d("StompRepo", "Sent STOMP DISCONNECT frame.")
        }

        webSocketService.disconnect() // WebSocket 연결 해제 요청
        messageFlowJob?.cancel() // Flow 작업 취소
        messageFlow = null
        currentRoomId = null
        currentTopic = null
        currentSubscriptionId = null
        _connectionState.value = StompConnectionState.DISCONNECTED // 최종 상태
        Log.i("StompRepo", "Disconnect handling complete.")
    }
}