package com.d104.data.remote.datasource.YogaPoseHistoryDataSource

import com.d104.data.remote.dto.BestPoseHistoryResponseDto
import com.d104.data.remote.dto.PoseRecordRequest
import com.d104.data.remote.dto.PoseRecordResponse
import com.d104.data.remote.dto.YogaPoseHistoryDetailResponseDto
import okhttp3.MultipartBody
import retrofit2.Response

interface YogaPoseHistoryDataSource {
    suspend fun postYogaPoseHistory(poseId:Long, poseRecordRequest:PoseRecordRequest, recordImg:MultipartBody.Part): Response<PoseRecordResponse>
    suspend fun getYogaBestHistories():Response<List<BestPoseHistoryResponseDto>>
    suspend fun getYogaPoseHistoryDetail(poseId:Long):Response<YogaPoseHistoryDetailResponseDto>
}