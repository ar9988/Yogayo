package com.d104.domain.usecase

import com.d104.domain.model.SignalingMessage
import com.d104.domain.model.UserJoinedMessage
import com.d104.domain.model.UserLeftMessage
import com.d104.domain.model.UserReadyMessage
import com.d104.domain.repository.DataStoreRepository
import com.d104.domain.repository.WebSocketRepository
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import javax.inject.Inject

class SendSignalingMessageUseCase @Inject constructor(
    private val webSocketRepository: WebSocketRepository,
    private val dataStoreRepository: DataStoreRepository,
    private val json:Json
){
    suspend operator fun invoke(destination: String, type: Int): Boolean { // 반환 타입을 Boolean으로 변경하여 성공 여부 전달
        return try {
            val user = dataStoreRepository.getUser().first() ?: run {
                return false // 사용자 정보 없으면 실패 반환
            }

            // 보낼 메시지 객체를 담을 변수 (타입은 부모 클래스)
            val messageToSend: SignalingMessage

            // 타입에 따라 적절한 메시지 객체 생성 및 할당
            when (type) {
                0 -> { // Join
                    messageToSend = UserJoinedMessage(
                        peerId = user.userId.toString(),
                        userNickName = user.userNickname,
                        type = "user_joined" // 또는 data class @SerialName 통해 자동 설정
                    )
                }
                1 -> { // Ready
                    messageToSend = UserReadyMessage(
                        peerId = user.userId.toString(),
                        isReady = true,
                        type = "user_ready"
                    )
                }
                2 -> { // Not Ready
                    messageToSend = UserReadyMessage(
                        peerId = user.userId.toString(),
                        isReady = false,
                        type = "user_not_ready" // type 명확히 구분 필요
                    )
                }
                3 -> { // Left (주석은 4였으나 코드는 3)
                    messageToSend = UserLeftMessage(
                        peerId = user.userId.toString(),
                        type = "user_left"
                    )
                 }
                else -> {
                    // 알 수 없는 타입 처리
                    return false // 알 수 없는 타입이면 실패 반환
                }
            }

            // 생성된 메시지 객체를 JSON으로 직렬화 (다형성 사용)
            val messageJson = json.encodeToString(SignalingMessage.serializer(), messageToSend)
            // WebSocket으로 전송
            val success = webSocketRepository.send(destination, messageJson)
            success // 최종 전송 성공 여부 반환

        } catch (e: Exception) {
            false // 오류 발생 시 실패 반환
        }
    }
}
// 0 join 1 ready 2 unready 4 left