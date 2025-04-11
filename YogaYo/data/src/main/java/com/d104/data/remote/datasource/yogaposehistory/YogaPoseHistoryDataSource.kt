package com.d104.data.remote.datasource.yogaposehistory

import com.d104.data.remote.dto.BestPoseHistoryResponseDto
import com.d104.data.remote.dto.MultiAllPhotoResponseDto
import com.d104.data.remote.dto.MultiBestPhotoResponseDto
import com.d104.data.remote.dto.PoseRecordRequestDto
import com.d104.data.remote.dto.PoseRecordResponseDto
import com.d104.data.remote.dto.YogaPoseHistoryDetailResponseDto
import okhttp3.MultipartBody
import retrofit2.Response

interface YogaPoseHistoryDataSource {
    suspend fun postYogaPoseHistory(poseId:Long, poseRecordRequestDto:PoseRecordRequestDto, recordImg:MultipartBody.Part): Response<PoseRecordResponseDto>
    suspend fun getYogaBestHistories():Response<List<BestPoseHistoryResponseDto>>
    suspend fun getYogaPoseHistoryDetail(poseId:Long):Response<YogaPoseHistoryDetailResponseDto>
    suspend fun getMultiBestPhoto(roomId: Long) :Response<List<MultiBestPhotoResponseDto>>
    suspend fun getMultiAllPhoto(roomId: Long, poseOrder: Int) : Response<List<MultiAllPhotoResponseDto>>
}