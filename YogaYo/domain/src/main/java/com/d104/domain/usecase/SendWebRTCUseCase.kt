package com.d104.domain.usecase

import com.d104.domain.model.DataChannelMessage
import com.d104.domain.repository.WebRTCRepository
import com.d104.domain.utils.toByteArray
import kotlinx.serialization.json.Json
import javax.inject.Inject

class SendWebRTCUseCase @Inject constructor(
    private val webRTCRepository: WebRTCRepository,
    private val json:Json
) {
    suspend operator fun invoke(message: DataChannelMessage) {
        webRTCRepository.sendBroadcastData(
            message.toByteArray(json)
        )
    }
}