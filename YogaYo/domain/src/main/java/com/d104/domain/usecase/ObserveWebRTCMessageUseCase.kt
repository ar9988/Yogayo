package com.d104.domain.usecase

import com.d104.domain.model.DataChannelMessage
import com.d104.domain.repository.WebRTCRepository
import jdk.internal.net.http.common.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import javax.inject.Inject

class ObserveWebRTCMessageUseCase @Inject constructor(
    private val webRTCRepository: WebRTCRepository,
    private val json: Json
) {
    operator fun invoke(): Flow<Pair<String, DataChannelMessage>> {
        return webRTCRepository.observeAllReceivedData()
            .map { (id, byteArray) ->
                try {
                    val message = byteArray.decodeToString()
                    println("USECASE!: Attempting to decode JSON: $message") // 디버깅 시 로그 추가
                    val dataChannelMessage = json.decodeFromString<DataChannelMessage>(message)
                    println("USECASE!: Successfully decoded message from $id as ${dataChannelMessage::class.simpleName}")
                    id to dataChannelMessage // 성공 시 Pair 반환
                } catch (e: SerializationException) { // Kotlinx Serialization 관련 예외 처리
                    println("USECASE!: DECODING FAILED (SerializationException) for message from peer $id. Message snippet: ${e.message?.take(100)}...")
                    null // 실패 시 null 반환
                } catch (e: Exception) { // 기타 예외 처리 (예: decodeToString 실패 등)
                    println("USECASE!: Exception (e) for message from peer $id. Message snippet: ${e.message?.take(100)}...")
                    null // 실패 시 null 반환
                }
            }
            .filterNotNull() // map에서 null이 반환된 경우 Flow에서 제외시킴
        // 또는 catch 연산자를 map 뒤에 사용하여 Flow 전체의 에러 처리
        /*
        .catch { e ->
            Timber.e(e, "Error in ObserveWebRTCMessageUseCase Flow")
            // 여기서 에러를 로깅하고 Flow를 계속 진행시킬지, 아니면 재시도 로직 등을 넣을지 결정
            // emit(null) 같은 것을 하거나, 아무것도 안 하면 Flow 종료될 수 있음
        }
        */
    }
}