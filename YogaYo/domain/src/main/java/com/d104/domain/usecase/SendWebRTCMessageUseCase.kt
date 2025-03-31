package com.d104.domain.usecase

import com.d104.domain.repository.WebRTCRepository
import javax.inject.Inject

class SendWebRTCMessageUseCase @Inject constructor(
    private val webRTCRepository: WebRTCRepository
) {
    suspend operator fun invoke(dataType:Int,message: String) {
        webRTCRepository.sendBroadcastData(
            dataType,
            message.toByteArray()
        )
    }
}