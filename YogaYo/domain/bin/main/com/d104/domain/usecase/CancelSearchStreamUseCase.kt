package com.d104.domain.usecase

import com.d104.domain.repository.LobbyRepository
import javax.inject.Inject

class CancelSearchStreamUseCase @Inject constructor(
    private val lobbyRepository: LobbyRepository,
) {
    operator fun invoke() {
        lobbyRepository.stopSse()
    }
}