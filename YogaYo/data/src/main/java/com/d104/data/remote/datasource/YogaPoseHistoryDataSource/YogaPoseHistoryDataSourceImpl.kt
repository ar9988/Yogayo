package com.d104.data.remote.datasource.YogaPoseHistoryDataSource

import com.d104.data.remote.api.YogaPoseHistoryApiService
import com.d104.data.remote.dto.BestPoseHistoryResponseDto
import com.d104.data.remote.dto.PoseRecordRequest
import com.d104.data.remote.dto.PoseRecordResponse
import com.d104.data.remote.dto.YogaPoseHistoryDetailResponseDto
import okhttp3.MultipartBody
import retrofit2.Response
import javax.inject.Inject

class YogaPoseHistoryDataSourceImpl @Inject constructor(
    private val yogaPoseHistoryApiService: YogaPoseHistoryApiService
) :YogaPoseHistoryDataSource{
    override suspend fun postYogaPoseHistory(
        poseId: Long,
        poseRecordRequest: PoseRecordRequest,
        recordImg: MultipartBody.Part
    ): Response<PoseRecordResponse> = yogaPoseHistoryApiService.postYogaPoseHistory(
        poseId = poseId,
        poseRecordRequest = poseRecordRequest,
        recordImg = recordImg
    )

    override suspend fun getYogaBestHistories(): Response<List<BestPoseHistoryResponseDto>> = yogaPoseHistoryApiService.getYogaBestHistories()

    override suspend fun getYogaPoseHistoryDetail(poseId:Long): Response<YogaPoseHistoryDetailResponseDto> = yogaPoseHistoryApiService.getYogaPoseHistoryDetail(poseId)
}