package com.d104.domain.usecase

import com.d104.domain.repository.WebSocketRepository
import javax.inject.Inject

class CloseWebSocketUseCase @Inject constructor(
    private val webSocketRepository: WebSocketRepository
) {
    operator fun invoke() {
        webSocketRepository.disconnect()
    }
}