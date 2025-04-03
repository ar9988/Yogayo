package com.d104.domain.usecase

import com.d104.domain.model.DataChannelMessage
import com.d104.domain.repository.WebRTCRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import javax.inject.Inject

class ObserveWebRTCMessageUseCase @Inject constructor(
    private val webRTCRepository: WebRTCRepository,
    private val json:Json
) {
    operator fun invoke(): Flow<Pair<String,DataChannelMessage>> {
        return webRTCRepository.observeAllReceivedData()
            .map { (id, byteArray) ->
                val message = byteArray.decodeToString() // ByteArray를 String으로 변환
                val dataChannelMessage =
                    json.decodeFromString<DataChannelMessage>(message) // JSON 파싱
                id to dataChannelMessage // Pair로 반환
            }
    }
}