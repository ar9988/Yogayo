package com.d104.domain.usecase

import com.d104.domain.repository.ImageReassemblyRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveChunkImageUseCase @Inject constructor(
    private val imageReassemblyRepository: ImageReassemblyRepository
) {
    operator fun invoke(): Flow<ByteArray> {
        return imageReassemblyRepository.observeImage()
    }
}