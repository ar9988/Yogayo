package com.d104.data.repository

import android.util.Log
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
    private val webSocketService: WebSocketService
) : WebSocketRepository {

    private val webSocketUrl = "ws://j12d104.p.ssafy.io/ws/chat" // 엔드포인트 확인
    // private val host = "j12d104.p.ssafy.io" // CONNECT 프레임 호스트 확인

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
            Log.d("StompRepo", "WebSocket Opened. Sending STOMP CONNECT...")
            val connectFrame = StompUtils.buildConnectFrame("j12d104.p.ssafy.io") // 호스트 확인
            val sent = webSocketService.send(connectFrame)
            if (!sent) {
                Log.e("StompRepo", "Failed to send STOMP CONNECT frame.")
                handleConnectionFailure(Exception("Failed to send CONNECT frame"))
            }
            // CONNECTING 상태는 connectAndSubscribe에서 설정
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            // 메시지 처리는 channelFlow 내부에서 수행됨 (아래 connectAndSubscribe 참고)
            Log.d("StompRepo", "Raw Frame Received (forwarded to flow): ${text.take(100)}...")
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            Log.w("StompRepo", "WebSocket Closing: Code=$code, Reason=$reason")
            handleDisconnect("WebSocket Closing")
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            Log.w("StompRepo", "WebSocket Closed: Code=$code, Reason=$reason")
            handleDisconnect("WebSocket Closed")
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.e("StompRepo", "WebSocket Failure: ${t.message}", t)
            handleConnectionFailure(t)
        }
    }

    override suspend fun connect(topic:String): Flow<String> {
        // 0. 이미 동일한 방에 연결되어 있고 Flow가 있다면 즉시 반환
        if (currentRoomId == topic && _connectionState.value == StompConnectionState.CONNECTED && messageFlow != null) {
            Log.d("StompRepo", "Already connected to room $topic. Returning existing flow.")
            return messageFlow!! // non-null 보장
        }

        // 1. 다른 방에 연결되어 있다면 먼저 해제
        if (_connectionState.value != StompConnectionState.DISCONNECTED && currentRoomId != topic) {
            Log.w("StompRepo", "Switching rooms. Disconnecting from $currentRoomId first.")
            disconnect()
            // disconnect()가 상태를 DISCONNECTED로 변경하므로 잠시 기다리거나 상태 변경을 기다림
            _connectionState.first { it == StompConnectionState.DISCONNECTED } // 상태 변경 기다림
        }

        // 2. 상태 초기화 및 연결 시작
        _connectionState.value = StompConnectionState.CONNECTING
        currentRoomId = topic
        currentTopic = "/topic/room/$topic" // 방 ID 기반 토픽 경로 생성 (서버와 협의 필요)
        currentSubscriptionId = "sub-$topic-${UUID.randomUUID().toString().take(8)}" // 방별 고유 ID
        messageFlowJob?.cancel() // 이전 Flow 작업 취소

        // 3. 메시지를 처리할 Flow 생성 및 공유 설정
        val internalFlow = channelFlow<String> {
            val forwardingListener = object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    webSocketListener.onOpen(webSocket, response) // CONNECT 프레임 전송 요청
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    // Raw STOMP 프레임 처리
                    try {
                        val (command, headers, body) = StompUtils.parseFrame(text)
                        Log.d("StompRepo", "Processing Frame: Command=$command, SubId=${headers["subscription"]}")

                        when (command) {
                            "CONNECTED" -> {
                                Log.i("StompRepo", "STOMP CONNECTED frame received.")
                                _connectionState.value = StompConnectionState.CONNECTED
                                // 연결 성공 시 즉시 구독 프레임 전송
                                sendSubscriptionFrame(currentTopic!!, currentSubscriptionId!!)
                            }
                            "MESSAGE" -> {
                                // 현재 구독 ID와 일치하는 메시지만 Flow로 전달
                                if (headers["subscription"] == currentSubscriptionId) {
                                    Log.d("StompRepo", "Message for current subscription received.")
                                    trySend(body)
                                } else {
                                    Log.w("StompRepo", "Received message for unrelated subscription: ${headers["subscription"]}")
                                }
                            }
                            "ERROR" -> {
                                Log.e("StompRepo", "STOMP ERROR frame received: $headers - $body")
                                // 오류 발생 시 Flow 종료
                                close(StompErrorException("STOMP Error: ${headers["message"]} - $body"))
                            }
                            // PONG (Heartbeat 응답) 등 다른 프레임 처리 필요 시 추가
                        }
                    } catch (e: Exception) {
                        Log.e("StompRepo", "Error parsing STOMP frame: ${e.message}", e)
                        // 파싱 오류 시 Flow 종료 고려
                        close(e)
                    }
                }

                override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                    webSocketListener.onClosing(webSocket, code, reason)
                    close()
                }
                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    webSocketListener.onClosed(webSocket, code, reason)
                    close()
                }
                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    webSocketListener.onFailure(webSocket, t, response)
                    close(t) // Flow를 에러와 함께 종료
                }
            }

            // WebSocket 연결 시작
            Log.d("StompRepo", "Connecting WebSocket to $webSocketUrl for room $topic")
            webSocketService.connect(webSocketUrl, forwardingListener)

            // Flow 종료 시 정리 작업
            awaitClose {
                Log.d("StompRepo", "ChannelFlow for room $topic closing.")
                // disconnectInternal() 호출은 disconnect() 또는 리스너 콜백에서 처리
            }
        }

        // 생성된 Flow를 공유 가능하게 만들고 저장
        messageFlow = internalFlow.shareIn(repositoryScope, SharingStarted.WhileSubscribed())
        // Flow 관찰 시작 (오류 처리 및 재시작 로직 추가 가능)
        messageFlowJob = repositoryScope.launch {
            messageFlow?.catch { e ->
                Log.e("StompRepo", "Error in shared message flow", e)
                handleConnectionFailure(e) // 공유 Flow에서 에러 발생 시 처리
            }?.collect{} // 공유 Flow 활성화 및 메시지 처리 시작
        }

        // 연결 상태가 CONNECTED가 될 때까지 기다리거나 타임아웃 처리 (선택적)
        try {
            withTimeoutOrNull(15000) { // 15초 타임아웃 예시
                _connectionState.first { it == StompConnectionState.CONNECTED }
            }
            if (_connectionState.value != StompConnectionState.CONNECTED) {
                throw Exception("STOMP connection timed out for room $topic")
            }
        } catch (e: Exception) {
            Log.e("StompRepo", "Connection failed or timed out for room $topic", e)
            handleConnectionFailure(e)
            throw e // 실패를 호출자에게 알림
        }

        Log.i("StompRepo", "Successfully connected and subscribed to room $topic. Returning message flow.")
        return messageFlow!! // 위에서 null 체크/할당 완료
    }

    override fun send(destination: String, message: String): Boolean {
        if (_connectionState.value != StompConnectionState.CONNECTED) {
            Log.w("StompRepo", "Cannot send message, STOMP not connected.")
            return false
        }
        // '/app/...' 형태의 destination 확인 필요
        val sendFrame = StompUtils.buildSendFrame("app/room/"+destination, message)
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