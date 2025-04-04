package com.d104.domain.usecase

import com.d104.domain.repository.WebRTCRepository
import javax.inject.Inject

class InitiateConnectionUseCase @Inject constructor(
    private val webRTCRepository: WebRTCRepository
) {
    suspend operator fun invoke(fromPeerId:String,peerId:String){
        webRTCRepository.startConnection(fromPeerId,peerId)
    }
}