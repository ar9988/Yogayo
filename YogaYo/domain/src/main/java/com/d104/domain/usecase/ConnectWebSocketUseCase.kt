package com.d104.domain.usecase

import com.d104.domain.model.SignalingMessage
import com.d104.domain.repository.WebSocketRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.serialization.json.Json
import javax.inject.Inject

class ConnectWebSocketUseCase @Inject constructor(
    private val webSocketRepository: WebSocketRepository,
    private val json: Json
) {
    suspend operator fun invoke(topic:String) : Flow<SignalingMessage> {
        return webSocketRepository.connect(topic)
            .mapNotNull { messageJson->
                try {
                    val signalingMessage = json.decodeFromString<SignalingMessage>(messageJson)
                    signalingMessage
                } catch (e: Exception) {
                    null
                }
            }
            .catch { e ->
            }
    }
}