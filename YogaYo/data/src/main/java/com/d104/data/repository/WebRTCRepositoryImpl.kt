package com.d104.data.repository

import android.util.Log
import com.d104.data.remote.manager.WebRTCManager
import com.d104.domain.model.AnswerMessage
import com.d104.domain.model.IceCandidateMessage
import com.d104.domain.model.OfferMessage
import com.d104.domain.model.RoomPeersMessage
import com.d104.domain.model.SignalingMessage
import com.d104.domain.model.UserJoinedMessage
import com.d104.domain.model.UserLeftMessage
import com.d104.domain.model.WebRTCConnectionState
import com.d104.domain.repository.DataStoreRepository
import com.d104.domain.repository.WebRTCRepository
import com.d104.domain.repository.WebSocketRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.webrtc.PeerConnection
import javax.inject.Inject

class WebRTCRepositoryImpl @Inject constructor(
    private val dataStoreRepository: DataStoreRepository,
    private val webRTCManager: WebRTCManager,         // 실제 WebRTC 로직 담당
    private val webSocketRepository: WebSocketRepository, // 시그널링 메시지 전송/수신
    private val json: Json,                           // JSON 직렬화/역직렬화 (Hilt 등으로 주입)
    private val appScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) : WebRTCRepository {

    private val TAG = "WebRTCRepositoryImpl"

    // 초기화 블록: WebRTCManager 의 Flow 를 구독하여 시그널링 메시지 전송 로직 연결
    init {
        Log.d(TAG, "Initializing WebRTCRepositoryImpl and subscribing to WebRTCManager flows.")
        // WebRTCManager 에서 발생하는 시그널링 메시지를 WebSocket 을 통해 전송
        appScope.launch {
            webRTCManager.outgoingSignalingMessage.collect { signalingMessage ->
                try {
                    val messageJson = json.encodeToString(SignalingMessage.serializer(), signalingMessage) // 다형성 직렬화
                    val destination = determineSignalingDestination() // 메시지 타입과 수신자 ID 기반으로 목적지 결정

                    if (destination != null) {
                        Log.d(TAG, "Sending signaling message of type ${signalingMessage::class.simpleName} to $destination")
                        webSocketRepository.send(destination, messageJson)
                    } else {
                        Log.w(TAG, "Could not determine destination for signaling message: $signalingMessage")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to encode or send signaling message", e)
                    // TODO: 오류 처리 (예: 특정 Flow 로 오류 상태 전파)
                }
            }
        }
    }

    // WebRTCManager 에서 발생하는 연결 상태 변경 Flow 노출
    // peerId 에 해당하는 Flow 를 찾아 반환해야 함 (간단한 예시는 아래 참고)
    // 좀 더 복잡한 구현: Map<String, Flow> 를 관리하거나, Manager 의 Flow 를 필터링
    override fun observeConnectionEvents(peerId: String): Flow<WebRTCConnectionState> {
        // WebRTCManager 의 SharedFlow 를 필터링하여 특정 peerId 에 대한 상태만 전달
        return webRTCManager.peerConnectionStateChanged
            .filter { it.peerId == peerId }
            .map { mapToDomainState(it.state) } // Manager 의 상태를 Domain 상태로 변환
            .distinctUntilChanged() // 동일한 상태 변경은 한번만 전달
    }

    // WebRTCManager 에서 발생하는 데이터 수신 Flow 노출
    override fun observeReceivedData(peerId: String): Flow<ByteArray> {
        // WebRTCManager 의 SharedFlow 를 필터링하여 특정 peerId 로부터 온 데이터만 전달
        return webRTCManager.receivedData
            .filter { it.peerId == peerId }
            .map { it.data }
    }

    // 연결 시작 요청 (Offer 생성 트리거)
    override suspend fun startConnection(fromPeerId:String,peerId: String): Result<Unit> {
        return try {
            Log.d(TAG, "Requesting start connection to peer: $peerId")
            webRTCManager.initiateConnection(fromPeerId ,peerId)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start connection to peer: $peerId", e)
            Result.failure(e)
        }
    }

    // 특정 피어에게 데이터 전송
    override suspend fun sendData(peerId: String, data: ByteArray): Result<Unit> {
        return try {
            webRTCManager.sendData(peerId, data)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send data to peer: $peerId", e)
            Result.failure(e)
        }
    }

    // 모든 연결된 피어에게 데이터 브로드캐스트
    override suspend fun sendBroadcastData(data: ByteArray): Result<Unit> {
        return try {
            webRTCManager.broadcastData(data)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to broadcast data", e)
            Result.failure(e)
        }
    }

    override fun handleSignalingMessage(message: SignalingMessage) {
        appScope.launch { // 코루틴 스코프 내에서 사용자 ID를 비동기적으로 가져올 수 있도록 함
            val currentUserId = getCurrentUserId() // 현재 사용자 ID를 가져오는 로직 (비동기일 수 있음)

            if (currentUserId == null) {
                Log.e(TAG, "Cannot handle signaling message, current user ID is null.")
                return@launch
            }

            // --- 자기 자신이 보낸 메시지인지 확인 ---
            if (message.fromPeerId == currentUserId) {
                Log.w(TAG, "Received a message from self, ignoring: ${message::class.simpleName} from ${message.fromPeerId}")
                return@launch // 자신이 보낸 메시지면 처리하지 않고 종료
            }
            // -----------------------------------

            Log.d(TAG, "Handling signaling message of type ${message::class.simpleName} from peer ${message.fromPeerId}")
            try {
                when (message) {
                    is OfferMessage -> {
                        // Offer는 수신자 ID (toPeerId)가 자신이어야 함
                        if (message.toPeerId == currentUserId) {
                            webRTCManager.onOfferReceived(currentUserId, message.fromPeerId, message.sdp) // toPeerId가 내가 맞으므로 fromPeerId를 전달
                        } else {
                            Log.w(TAG, "Received Offer not addressed to me (to: ${message.toPeerId}, me: $currentUserId), ignoring.")
                        }
                    }
                    is AnswerMessage -> {
                        // Answer는 수신자 ID (toPeerId)가 자신이어야 함
                        if (message.toPeerId == currentUserId) {
                            webRTCManager.onAnswerReceived(message.fromPeerId, message.sdp) // Answer를 보낸 사람의 ID 전달
                        } else {
                            Log.w(TAG, "Received Answer not addressed to me (to: ${message.toPeerId}, me: $currentUserId), ignoring.")
                        }
                    }
                    is IceCandidateMessage -> {
                        // Candidate는 수신자 ID (toPeerId)가 자신이어야 함
                        if (message.toPeerId == currentUserId) {
                            webRTCManager.onIceCandidateReceived(message.fromPeerId, message.candidate) // Candidate를 보낸 사람의 ID 전달
                        } else {
                            Log.w(TAG, "Received ICE Candidate not addressed to me (to: ${message.toPeerId}, me: $currentUserId), ignoring.")
                        }
                    }
                    // RoomPeersMessage, UserJoinedMessage, UserLeftMessage 등은 fromPeerId 체크가 의미 없을 수 있음 (서버가 보내는 정보)
                    // 필요에 따라 해당 메시지 타입에도 fromPeerId 체크 추가 가능
                    is RoomPeersMessage -> {
                        Log.i(TAG, "Received room peers: ${message.peerIds}")
                        // TODO: 처리 로직
                    }
                    is UserJoinedMessage -> {
                        Log.i(TAG, "User joined: ${message.fromPeerId}")
                        // UserJoined 메시지의 peerId가 나 자신인지 확인하는 것은 의미가 있을 수 있음
                        if (message.fromPeerId == currentUserId) {
                            Log.d(TAG, "Ignoring self join message.")
                            return@launch
                        }
                        // TODO: 처리 로직
                    }
                    is UserLeftMessage -> {
                        Log.i(TAG, "User left: ${message.fromPeerId}")
                        // TODO: 처리 로직
                        disconnect(message.fromPeerId)
                    }
                    else -> Log.w(TAG, "Unhandled signaling message type: ${message::class.simpleName}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error handling signaling message", e)
                // TODO: 오류 처리
            }
        }
    }

    // WebRTCRepositoryImpl 내부에 또는 별도의 UseCase 등으로 구현 필요
    private suspend fun getCurrentUserId(): String? {
        // 예시: DataStoreRepository 를 사용하여 사용자 ID 가져오기
        return try {
            dataStoreRepository.getUserId()
            // 또는 dataStoreRepository.getUserId().first() 등
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get current user ID", e)
            null
        }
    }

    // 특정 피어와의 연결 종료
    override fun disconnect(peerId: String) {
        Log.d(TAG, "Requesting disconnect from peer: $peerId")
        webRTCManager.closeConnection(peerId)
    }

    // 모든 연결 종료 및 자원 해제
    override fun disconnectAll() {
        Log.d(TAG, "Requesting disconnect all peers and release resources.")
        webRTCManager.release() // WebRTCManager 의 자원 해제 함수 호출
        // WebSocket 연결 해제는 별도로 호출해야 할 수 있음 (ViewModel 등에서)
        // webSocketRepository.disconnect()
    }

    override fun initializeWebRTC() {
        webRTCManager.initialize() // WebRTCManager 초기화 함수 호출
    }

    override fun observeAllReceivedData(): Flow<Pair<String, ByteArray>> {
        return webRTCManager.receivedData
            .map { Pair(it.peerId, it.data) }
    }

    private fun determineSignalingDestination(): String {
        val roomId = webSocketRepository.getCurrentRoomId() // 현재 방 ID 가져오기
        val destinationBase = "$roomId"
        return destinationBase
        // 메시지 타입에 따라 수신자 ID 가 필요한 경우 toPeerId 를 경로에 추가
//        return when (message) {
//            is OfferMessage -> "$destinationBase/${message.toPeerId}"
//            is AnswerMessage -> "$destinationBase/${message.toPeerId}"
//            is IceCandidateMessage -> "$destinationBase/${message.toPeerId}"
//            // 다른 타입의 메시지는 특정 대상이 없을 수 있으므로 기본 목적지 사용 (필요 시)
//            // 예를 들어, 방 전체에 보내는 메시지가 있다면 다른 경로 사용
//            else -> {
//                Log.w(TAG, "Cannot determine specific destination for message type: ${message::class.simpleName}, using base destination $destinationBase")
//                // 만약 특정 대상 없이 방 전체나 서버로 보내는 메시지라면 기본 목적지 반환 또는 null 반환
//                null // 혹은 destinationBase 만 반환할 수도 있음 (서버 구현에 따라 다름)
//            }
//        }
    }

    private fun mapToDomainState(state: PeerConnection.IceConnectionState): WebRTCConnectionState {
        return when (state) {
            PeerConnection.IceConnectionState.NEW -> WebRTCConnectionState.NEW
            PeerConnection.IceConnectionState.CHECKING -> WebRTCConnectionState.CONNECTING
            PeerConnection.IceConnectionState.CONNECTED -> WebRTCConnectionState.CONNECTED
            PeerConnection.IceConnectionState.COMPLETED -> WebRTCConnectionState.CONNECTED // Completed도 Connected로 간주
            PeerConnection.IceConnectionState.DISCONNECTED -> WebRTCConnectionState.DISCONNECTED
            PeerConnection.IceConnectionState.FAILED -> WebRTCConnectionState.FAILED
            PeerConnection.IceConnectionState.CLOSED -> WebRTCConnectionState.CLOSED
            // else -> WebRTCConnectionState.NEW // enum 에는 else 가 필요 없음
        }
    }
}