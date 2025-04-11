package com.d104.domain.usecase

import com.d104.domain.repository.WebSocketRepository
import javax.inject.Inject

class ObserveWebSocketConnectionStateUseCase @Inject constructor(
    private val webSocketRepository: WebSocketRepository
) {
    operator fun invoke() = webSocketRepository.connectionState
}