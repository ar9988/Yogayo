package com.d104.domain.usecase

import com.d104.domain.repository.YogaPoseHistoryRepository
import javax.inject.Inject

class GetYogaPoseHistoryDetailUseCase @Inject constructor(
    private val yogaPoseHistoryRepository: YogaPoseHistoryRepository

) {
    suspend operator fun invoke(poseId: Long) = yogaPoseHistoryRepository.getYogaPoseHistoryDetail(poseId)

}