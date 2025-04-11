package com.d104.domain.usecase

import com.d104.domain.model.YogaHistory
import com.d104.domain.repository.YogaPoseHistoryRepository
import javax.inject.Inject

class PostYogaPoseHistoryUseCase @Inject constructor(
    private val yogaPoseHistoryRepository: YogaPoseHistoryRepository
){

    suspend operator fun invoke(poseId:Long, roomRecordId:Long?=null, accuracy:Float, ranking:Int?=null, poseTime:Float, imgUri:String)= yogaPoseHistoryRepository.postYogaPoseHistory(
        poseId = poseId,
        roomRecordId = roomRecordId,
        accuracy = accuracy,
        ranking = ranking,
        poseTime = poseTime,
        imgUri = imgUri
    )
}