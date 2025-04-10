package com.d104.domain.usecase

import com.d104.domain.model.MissingChunksInfo
import com.d104.domain.repository.ImageReassemblyRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveMissingChunksUseCase @Inject constructor(
    private val imageReassemblyRepository: ImageReassemblyRepository
) {
    operator fun invoke(): Flow<MissingChunksInfo> {
        return imageReassemblyRepository.observeMissingChunks()
    }
}