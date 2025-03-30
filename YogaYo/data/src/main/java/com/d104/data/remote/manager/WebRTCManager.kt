package com.d104.data.remote.manager


import android.content.Context
import android.util.Log
import com.d104.domain.model.AnswerMessage
import com.d104.domain.model.IceCandidateData
import com.d104.domain.model.IceCandidateMessage
import com.d104.domain.model.OfferMessage
import com.d104.domain.model.SignalingMessage
import com.d104.domain.repository.DataStoreRepository
import com.d104.domain.repository.UserRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import org.webrtc.*
import java.nio.ByteBuffer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebRTCManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userRepository: UserRepository,
    private val dataStoreRepository: DataStoreRepository
    // 필요하다면 WebSocketRepository 를 직접 주입받거나,
    // 시그널링 메시지 전송을 위한 콜백 인터페이스를 정의하여 사용
) {
    private val TAG = "WebRTCManager"

    private val _myPeerId = MutableStateFlow<String?>(null)
    // 코루틴 스코프
    private val managerScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // WebRTC 초기화
    private var peerConnectionFactory: PeerConnectionFactory? = null
    private var eglBase: EglBase? = null

    // ICE 서버 설정 (Coturn 서버 정보)
    private val iceServers = listOf(
        PeerConnection.IceServer.builder("stun:YOUR_COTURN_IP:3478").createIceServer(), // 실제 IP로 변경
        PeerConnection.IceServer.builder("turn:YOUR_COTURN_IP:3478")    // 실제 IP로 변경
            .setUsername("your_turn_username")        // 설정한 사용자 이름으로 변경
            .setPassword("your_turn_password")          // 설정한 비밀번호로 변경
            .createIceServer()
    )

    // 피어 연결 관리 (Key: peerId)
    private val peerConnections = mutableMapOf<String, PeerConnection>()
    private val dataChannels = mutableMapOf<String, DataChannel>()

    // --- 외부로 상태/이벤트를 알리기 위한 Flow ---
    // 생성된 ICE Candidate (시그널링 서버로 보내야 함)
    private val _outgoingSignalingMessage = MutableSharedFlow<SignalingMessage>()
    val outgoingSignalingMessage: SharedFlow<SignalingMessage> = _outgoingSignalingMessage.asSharedFlow()

    // P2P 연결 상태 변경
    data class PeerConnectionState(val peerId: String, val state: PeerConnection.IceConnectionState)
    private val _peerConnectionStateChanged = MutableSharedFlow<PeerConnectionState>()
    val peerConnectionStateChanged: SharedFlow<PeerConnectionState> = _peerConnectionStateChanged.asSharedFlow()

    // 데이터 채널을 통해 데이터 수신
    data class ReceivedData(val peerId: String, val data: ByteArray)
    private val _receivedData = MutableSharedFlow<ReceivedData>()
    val receivedData: SharedFlow<ReceivedData> = _receivedData.asSharedFlow()


    // --- 초기화 ---
    fun initialize() {
        Log.d(TAG, "Initializing PeerConnectionFactory...")
        val initializationOptions = PeerConnectionFactory.InitializationOptions.builder(context)
            .setEnableInternalTracer(true) // 상세 로그 활성화 (디버깅 시 유용)
            .createInitializationOptions()
        PeerConnectionFactory.initialize(initializationOptions)

        eglBase = EglBase.create() // EGL Context (비디오 사용 안해도 필요할 수 있음)
        val options = PeerConnectionFactory.Options() // 필요시 옵션 설정
        peerConnectionFactory = PeerConnectionFactory.builder()
            .setOptions(options)
            .setVideoEncoderFactory(DefaultVideoEncoderFactory(eglBase?.eglBaseContext, true, true)) // 비디오 사용 안해도 기본값 제공
            .setVideoDecoderFactory(DefaultVideoDecoderFactory(eglBase?.eglBaseContext)) // 비디오 사용 안해도 기본값 제공
            .createPeerConnectionFactory()
        Log.d(TAG, "PeerConnectionFactory initialized.")

        loadMyPeerId()
    }

    private fun loadMyPeerId() {
        managerScope.launch(Dispatchers.IO) {
            dataStoreRepository.getUser()
                .mapNotNull { it?.userId } // UserData 객체에서 userId 추출 (null 이 아닌 경우만)
                // 또는 dataStoreRepository.getUserId().filterNotNull()
                .catch { e ->
                    Log.e(TAG, "Failed to load Peer ID from DataStore", e)
                    // 필요하다면 오류 처리 또는 기본값 emit
                }
                .collectLatest { peerId -> // 최신 Peer ID 만 _myPeerId 에 업데이트
                    Log.d(TAG, "My Peer ID loaded: $peerId")
                    _myPeerId.value = peerId.toString()
                }
        }
    }

    // --- 연결 시작 (Offer 생성) ---
    fun initiateConnection(peerId: String) {
        managerScope.launch {
            Log.d(TAG, "Initiating connection to peer: $peerId")
            val peerConnection = createOrGetPeerConnection(peerId) ?: return@launch
            // 데이터 채널 생성 (Offer 생성 전에 해야 함)
            createDataChannel(peerId, peerConnection)

            // Offer 생성
            val sdpConstraints = MediaConstraints() // 필요시 오디오/비디오 제약조건 설정
            peerConnection.createOffer(object : SimpleSdpObserver() {
                override fun onCreateSuccess(sdp: SessionDescription?) {
                    sdp?.let { it ->
                        Log.d(TAG, "Offer created successfully for peer: $peerId")
                        peerConnection.setLocalDescription(SimpleSdpObserver(), it) // 로컬 SDP 설정
                        emitSignalingMessage { myPeerId -> // emitSignalingMessage 내부에서 null 체크 후 전달된 myPeerId 사용
                            OfferMessage(
                                fromPeerId = myPeerId, // StateFlow 의 값이 아닌, non-null ID 사용
                                toPeerId = peerId,
                                sdp = it.description, // it.description 사용
                                type = "offer"
                            )
                        }
                    } ?: run {
                        Log.e(TAG, "Offer creation success but SDP is null for peer: $peerId")
                    }
                }

                override fun onCreateFailure(error: String?) {
                    Log.e(TAG, "Failed to create offer for peer $peerId: $error")
                    // TODO: 오류 처리
                }
            }, sdpConstraints)
        }
    }

    // --- 시그널링 메시지 처리 ---

    fun onOfferReceived(peerId: String, sdp: String) {
        managerScope.launch {
            Log.d(TAG, "Received offer from peer: $peerId")
            val peerConnection = createOrGetPeerConnection(peerId) ?: return@launch

            // Remote Description 설정
            val sessionDescription = SessionDescription(SessionDescription.Type.OFFER, sdp)
            peerConnection.setRemoteDescription(object : SimpleSdpObserver() {
                override fun onSetSuccess() {
                    Log.d(TAG, "Remote description (offer) set successfully for peer: $peerId")
                    // Answer 생성
                    createAnswer(peerId, peerConnection)
                }

                override fun onSetFailure(error: String?) {
                    Log.e(TAG, "Failed to set remote description (offer) for peer $peerId: $error")
                    // TODO: 오류 처리
                }
            }, sessionDescription)
        }
    }

    private fun createAnswer(peerId: String, peerConnection: PeerConnection) {
        val sdpConstraints = MediaConstraints()

        peerConnection.createAnswer(object : SimpleSdpObserver() {
            override fun onCreateSuccess(sdp: SessionDescription?) {
                sdp?.let {
                    Log.d(TAG, "Answer created successfully for peer: $peerId")
                    peerConnection.setLocalDescription(SimpleSdpObserver(), it)
                    emitSignalingMessage { myPeerId -> // emitSignalingMessage 내부에서 null 체크 후 전달된 myPeerId 사용
                        AnswerMessage(
                            fromPeerId = myPeerId, // StateFlow 의 값이 아닌, non-null ID 사용
                            toPeerId = peerId,
                            sdp = it.description, // it.description 사용
                            type = "answer"
                        )
                    }
                } ?: run {
                    Log.e(TAG, "Answer creation success but SDP is null for peer: $peerId")
                }
            }

            override fun onCreateFailure(error: String?) {
                Log.e(TAG, "Failed to create answer for peer $peerId: $error")
                // TODO: 오류 처리
            }
        }, sdpConstraints)
    }

    fun onAnswerReceived(peerId: String, sdp: String) {
        managerScope.launch {
            Log.d(TAG, "Received answer from peer: $peerId")
            val peerConnection = peerConnections[peerId]
            if (peerConnection == null) {
                Log.e(TAG, "PeerConnection not found for peer: $peerId when receiving answer")
                return@launch
            }
            val sessionDescription = SessionDescription(SessionDescription.Type.ANSWER, sdp)
            peerConnection.setRemoteDescription(SimpleSdpObserver(), sessionDescription)
        }
    }

    fun onIceCandidateReceived(peerId: String, candidateData: IceCandidateData) {
        managerScope.launch {
            Log.d(TAG, "Received ICE candidate from peer: $peerId")
            val peerConnection = peerConnections[peerId]
            if (peerConnection == null) {
                Log.e(TAG, "PeerConnection not found for peer: $peerId when receiving ICE candidate")
                return@launch
            }
            val iceCandidate = IceCandidate(
                candidateData.sdpMid,
                candidateData.sdpMLineIndex,
                candidateData.sdpCandidate
            )
            peerConnection.addIceCandidate(iceCandidate)
        }
    }

    // --- 데이터 채널 ---
    private fun createDataChannel(peerId: String, peerConnection: PeerConnection) {
        Log.d(TAG, "Creating data channel for peer: $peerId")
        // 데이터 채널 설정 (필요에 따라 ordered, maxRetransmits 등 설정)
        val init = DataChannel.Init().apply {
            ordered = true // 메시지 순서 보장
        }
        val dataChannel = peerConnection.createDataChannel("dataChannel_$peerId", init)
        dataChannel?.let {
            registerDataChannelObserver(peerId, it)
            dataChannels[peerId] = it
            Log.d(TAG, "Data channel created for peer: $peerId")
        } ?: run {
            Log.e(TAG, "Failed to create data channel for peer: $peerId")
        }
    }

    private fun registerDataChannelObserver(peerId: String, dataChannel: DataChannel) {
        dataChannel.registerObserver(object : DataChannel.Observer {
            override fun onBufferedAmountChange(previousAmount: Long) {
                Log.d(TAG, "DataChannel $peerId: Buffered amount changed: $previousAmount -> ${dataChannel.bufferedAmount()}")
            }

            override fun onStateChange() {
                val state = dataChannel.state()
                Log.d(TAG, "DataChannel $peerId: State changed to $state")
                if (state == DataChannel.State.OPEN) {
                    // 데이터 채널 열림! 데이터 전송 가능
                } else if (state == DataChannel.State.CLOSED) {
                    // 데이터 채널 닫힘
                    dataChannels.remove(peerId) // 맵에서 제거
                }
            }

            override fun onMessage(buffer: DataChannel.Buffer?) {
                buffer?.let {
                    val data = ByteArray(it.data.remaining())
                    it.data.get(data)
                    Log.d(TAG, "DataChannel $peerId: Message received (${data.size} bytes)")
                    // 수신 데이터 Flow로 전달
                    managerScope.launch {
                        _receivedData.emit(ReceivedData(peerId, data))
                    }
                }
            }
        })
    }

    // --- 데이터 전송 ---
    fun sendData(peerId: String, data: ByteArray) {
        managerScope.launch {
            val dataChannel = dataChannels[peerId]
            if (dataChannel?.state() == DataChannel.State.OPEN) {
                val buffer = ByteBuffer.wrap(data)
                val sendResult = dataChannel.send(DataChannel.Buffer(buffer, false)) // false for text, true for binary
                if(sendResult){
                    Log.d(TAG,"Data sent successfully to $peerId (${data.size} bytes)")
                } else {
                    Log.e(TAG,"Failed to send data to $peerId")
                }
            } else {
                Log.w(TAG, "Data channel for $peerId is not open. Current state: ${dataChannel?.state()}")
            }
        }
    }

    fun broadcastData(data: ByteArray) {
        managerScope.launch {
            Log.d(TAG,"Broadcasting data to ${dataChannels.size} peers...")
            dataChannels.forEach { (peerId, dataChannel) ->
                if (dataChannel.state() == DataChannel.State.OPEN) {
                    val buffer = ByteBuffer.wrap(data)
                    dataChannel.send(DataChannel.Buffer(buffer, false))
                } else {
                    Log.w(TAG, "Skipping broadcast to $peerId, channel not open (${dataChannel.state()})")
                }
            }
        }
    }


    // --- 연결 관리 ---

    private fun createOrGetPeerConnection(peerId: String): PeerConnection? {
        return peerConnections[peerId] ?: run {
            Log.d(TAG, "Creating new PeerConnection for peer: $peerId")
            val rtcConfig = PeerConnection.RTCConfiguration(iceServers)
            // 필요시 추가 설정 (예: bundlePolicy, rtcpMuxPolicy)
            // rtcConfig.bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE
            // rtcConfig.rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE

            val peerConnection = peerConnectionFactory?.createPeerConnection(
                rtcConfig,
                object : PeerConnection.Observer {
                    override fun onSignalingChange(state: PeerConnection.SignalingState?) {
                        Log.d(TAG, "Peer $peerId: SignalingState changed: $state")
                    }

                    override fun onIceConnectionChange(newState: PeerConnection.IceConnectionState?) {
                        Log.d(TAG, "Peer $peerId: IceConnectionState changed: $newState")
                        newState?.let {
                            managerScope.launch {
                                _peerConnectionStateChanged.emit(PeerConnectionState(peerId, it))
                            }
                            if (it == PeerConnection.IceConnectionState.FAILED ||
                                it == PeerConnection.IceConnectionState.DISCONNECTED ||
                                it == PeerConnection.IceConnectionState.CLOSED) {
                                // 연결 끊김 처리
                                // closeConnection(peerId) // 자동으로 닫을지, 상위 레벨에서 처리할지 결정
                            }
                        }
                    }

                    override fun onIceConnectionReceivingChange(receiving: Boolean) {
                        Log.d(TAG, "Peer $peerId: IceConnectionReceivingChange: $receiving")
                    }

                    override fun onIceGatheringChange(newState: PeerConnection.IceGatheringState?) {
                        Log.d(TAG, "Peer $peerId: IceGatheringState changed: $newState")
                    }

                    override fun onIceCandidate(candidate: IceCandidate?) {
                        candidate?.let {
                            Log.d(TAG, "Peer $peerId: New ICE candidate generated: ${it.sdp.take(30)}...")
                            // ICE Candidate 메시지 생성 및 시그널링 채널로 전송 요청
                            val candidateData = IceCandidateData(
                                sdpMid = it.sdpMid,
                                sdpMLineIndex = it.sdpMLineIndex,
                                sdpCandidate = it.sdp
                            )
                            emitSignalingMessage {
                                IceCandidateMessage(
                                    fromPeerId = _myPeerId.toString(), // 로드된 ID 사용
                                    toPeerId = peerId,
                                    candidate = candidateData,
                                    type = "candidate"
                                )
                            }
                        }
                    }

                    override fun onIceCandidatesRemoved(candidates: Array<out IceCandidate>?) {
                        Log.d(TAG, "Peer $peerId: ICE candidates removed.")
                    }

                    override fun onAddStream(stream: MediaStream?) {
                        Log.d(TAG, "Peer $peerId: Stream added (deprecated).")
                        // 비디오/오디오 사용 시 여기 또는 onTrack 에서 처리
                    }

                    override fun onRemoveStream(stream: MediaStream?) {
                        Log.d(TAG, "Peer $peerId: Stream removed (deprecated).")
                    }

                    override fun onDataChannel(dc: DataChannel?) {
                        dc?.let {
                            Log.d(TAG, "Peer $peerId: DataChannel received: ${it.label()}")
                            registerDataChannelObserver(peerId, it)
                            dataChannels[peerId] = it
                        }
                    }

                    override fun onRenegotiationNeeded() {
                        Log.d(TAG, "Peer $peerId: Renegotiation needed.")
                        // 필요시 재협상 로직 구현 (예: 비디오 추가/제거 시)
                    }

                    override fun onAddTrack(receiver: RtpReceiver?, mediaStreams: Array<out MediaStream>?) {
                        Log.d(TAG, "Peer $peerId: Track added.")
                        // 비디오/오디오 트랙 수신 시 처리
                    }
                }
            )

            if (peerConnection == null) {
                Log.e(TAG, "Failed to create PeerConnection for peer: $peerId")
                null
            } else {
                peerConnections[peerId] = peerConnection
                peerConnection
            }
        }
    }

    // --- 자원 해제 ---
    fun closeConnection(peerId: String) {
        Log.d(TAG, "Closing connection for peer: $peerId")
        dataChannels[peerId]?.close()
        dataChannels.remove(peerId)
        peerConnections[peerId]?.close()
        peerConnections.remove(peerId)
    }

    fun closeAllConnections() {
        Log.d(TAG, "Closing all connections...")
        // 주의: Map 순회 중 제거 시 ConcurrentModificationException 발생 가능
        val peerIds = peerConnections.keys.toList() // 복사본 사용
        peerIds.forEach { closeConnection(it) }
        dataChannels.clear() // 안전하게 비우기
        peerConnections.clear() // 안전하게 비우기
    }

    fun release() {
        Log.d(TAG, "Releasing WebRTCManager resources...")
        closeAllConnections()
        peerConnectionFactory?.dispose()
        peerConnectionFactory = null
        eglBase?.release()
        eglBase = null
        Log.d(TAG, "WebRTCManager resources released.")
        // managerScope.cancel() // 필요하다면 스코프 취소
    }

    // --- 유틸리티 ---
    private fun emitSignalingMessage(messageBuilder: (myPeerId: String) -> SignalingMessage?) {
        managerScope.launch {
            // Peer ID 가 로드될 때까지 기다리거나, 로드되지 않았으면 로그 남기고 종료
            val myPeerId = _myPeerId.first { it != null } // null 이 아닐 때까지 기다림 (또는 다른 처리 방식)
            if (myPeerId == null) {
                Log.e(TAG, "Cannot emit signaling message, My Peer ID is not loaded yet.")
                return@launch
            }

            val message = messageBuilder(myPeerId) // 콜백을 사용하여 Peer ID 전달
            message?.let {
                Log.d(TAG, "Emitting signaling message: ${it::class.simpleName}")
                _outgoingSignalingMessage.emit(it)
            }
        }
    }

}

// WebRTC 콜백 단순화를 위한 헬퍼 클래스
open class SimpleSdpObserver : SdpObserver {
    override fun onCreateSuccess(sdp: SessionDescription?) {}
    override fun onCreateFailure(error: String?) {}
    override fun onSetSuccess() {}
    override fun onSetFailure(error: String?) {}
}