package com.d104.domain.usecase

import com.d104.domain.model.MultiBestPhoto
import com.d104.domain.repository.YogaPoseHistoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMultiBestPhotoUseCase @Inject constructor(
    private val yogaPoseHistoryRepository: YogaPoseHistoryRepository
) {
    suspend operator fun invoke(roomId:Long): Flow<Result<List<MultiBestPhoto>>> {
        return yogaPoseHistoryRepository.getMultiBestPhoto(roomId)
    }
}