package com.d104.domain.usecase

import com.d104.domain.repository.WebRTCRepository
import javax.inject.Inject

class SendMessageUseCase @Inject constructor(
    private val webRTCRepository: WebRTCRepository
) {
    suspend operator fun invoke(byteArray: ByteArray) = webRTCRepository.sendBroadcastData(
        dataType = 0,
        byteArray
    )
}