package com.d104.domain.usecase

import com.d104.domain.repository.WebRTCRepository
import javax.inject.Inject

class SendImageUseCase @Inject constructor(
    private val webRTCRepository: WebRTCRepository
) {
    suspend operator fun invoke(peerId:String, data:ByteArray) = webRTCRepository.sendBroadcastData(
        dataType = 1,
        data
    )
}