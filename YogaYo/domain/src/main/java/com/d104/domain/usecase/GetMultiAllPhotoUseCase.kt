package com.d104.domain.usecase

import com.d104.domain.model.MultiPhoto
import com.d104.domain.repository.YogaPoseHistoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMultiAllPhotoUseCase @Inject constructor(
    private val yogaPoseHistoryRepository: YogaPoseHistoryRepository
) {
    suspend operator fun invoke(roomId: Long, poseOrder:Int): Flow<Result<List<MultiPhoto>>> {
        return yogaPoseHistoryRepository.getMultiAllPhoto(roomId,poseOrder)
    }
}