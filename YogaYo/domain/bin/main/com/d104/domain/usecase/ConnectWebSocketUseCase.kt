package com.d104.domain.usecase

import com.d104.domain.repository.WebSocketRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ConnectWebSocketUseCase @Inject constructor(
    private val webSocketRepository: WebSocketRepository
) {
    suspend operator fun invoke(topic:String) : Flow<String> {
        return webSocketRepository.connect(topic)
    }
}