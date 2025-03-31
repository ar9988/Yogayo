package com.d104.domain.usecase

import com.d104.domain.repository.WebRTCRepository
import javax.inject.Inject

class ObserveWebRTCMessageUseCase @Inject constructor(
    private val webRTCRepository: WebRTCRepository
) {
    operator fun invoke() = webRTCRepository.observeAllReceivedData()
}