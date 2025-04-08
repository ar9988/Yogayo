package com.d104.domain.repository

import com.d104.domain.model.BestPoseRecord
import com.d104.domain.model.MultiBestPhoto
import com.d104.domain.model.MultiPhoto
import com.d104.domain.model.YogaPoseHistoryDetail
import com.d104.domain.model.YogaPoseRecord
import kotlinx.coroutines.flow.Flow

interface YogaPoseHistoryRepository {

    suspend fun postYogaPoseHistory(
        poseId:Long, roomRecordId:Long?,accuracy:Float,ranking:Int?,poseTime:Float, imgUri:String
    ): Flow<Result<YogaPoseRecord>>

    suspend fun getYogaBestHistories():Flow<Result<List<BestPoseRecord>>>

    suspend fun getYogaPoseHistoryDetail(poseId:Long): Flow<Result<YogaPoseHistoryDetail>>

    suspend fun getMultiBestPhoto(roomId:Long): Flow<Result<List<MultiBestPhoto>>>

    suspend fun getMultiAllPhoto(roomId:Long, poseIndex: Int): Flow<Result<List<MultiPhoto>>>
}