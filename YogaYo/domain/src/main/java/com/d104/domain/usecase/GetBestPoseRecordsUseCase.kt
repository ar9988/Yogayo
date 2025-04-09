package com.d104.domain.usecase

import com.d104.domain.repository.YogaPoseHistoryRepository
import javax.inject.Inject

class GetBestPoseRecordsUseCase @Inject constructor(
    private val yogaPoseHistoryRepository: YogaPoseHistoryRepository
){
    suspend operator fun invoke(roomId:Long) = yogaPoseHistoryRepository.getMultiBestPhoto(roomId)
}