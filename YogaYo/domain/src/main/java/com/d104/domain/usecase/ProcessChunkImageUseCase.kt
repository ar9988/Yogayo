package com.d104.domain.usecase

import com.d104.domain.model.ImageChunkMessage
import com.d104.domain.repository.ImageReassemblyRepository
import javax.inject.Inject

class ProcessChunkImageUseCase @Inject constructor(
    private val imageReassemblyRepository: ImageReassemblyRepository
) {
    operator fun invoke(msg: ImageChunkMessage){
        imageReassemblyRepository.processChunk(chunk = msg)
    }
}