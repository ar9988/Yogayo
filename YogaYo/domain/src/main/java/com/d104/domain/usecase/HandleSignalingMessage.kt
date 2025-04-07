package com.d104.domain.usecase

import com.d104.domain.model.SignalingMessage
import com.d104.domain.repository.WebRTCRepository
import javax.inject.Inject

class HandleSignalingMessage @Inject constructor(
    private val webRTCRepository: WebRTCRepository
) {
    operator fun invoke(message: SignalingMessage) {
        webRTCRepository.handleSignalingMessage(message)
    }
}