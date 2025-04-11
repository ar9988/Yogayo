package com.d104.data.remote.datasource.yogaposehistory

import com.d104.data.remote.api.YogaPoseHistoryApiService
import com.d104.data.remote.dto.BestPoseHistoryResponseDto
import com.d104.data.remote.dto.MultiAllPhotoResponseDto
import com.d104.data.remote.dto.MultiBestPhotoResponseDto
import com.d104.data.remote.dto.PoseRecordRequestDto
import com.d104.data.remote.dto.PoseRecordResponseDto
import com.d104.data.remote.dto.YogaPoseHistoryDetailResponseDto
import okhttp3.MultipartBody
import retrofit2.Response
import javax.inject.Inject

class YogaPoseHistoryDataSourceImpl @Inject constructor(
    private val yogaPoseHistoryApiService: YogaPoseHistoryApiService
) :YogaPoseHistoryDataSource{
    override suspend fun postYogaPoseHistory(
        poseId: Long,
        poseRecordRequestDto: PoseRecordRequestDto,
        recordImg: MultipartBody.Part
    ): Response<PoseRecordResponseDto> = yogaPoseHistoryApiService.postYogaPoseHistory(
        poseId = poseId,
        poseRecordRequestDto = poseRecordRequestDto,
        recordImg = recordImg
    )

    override suspend fun getYogaBestHistories(): Response<List<BestPoseHistoryResponseDto>> = yogaPoseHistoryApiService.getYogaBestHistories()

    override suspend fun getYogaPoseHistoryDetail(poseId:Long): Response<YogaPoseHistoryDetailResponseDto> = yogaPoseHistoryApiService.getYogaPoseHistoryDetail(poseId)
    override suspend fun getMultiBestPhoto(roomId: Long): Response<List<MultiBestPhotoResponseDto>> {
        return yogaPoseHistoryApiService.getMultiBestPhoto(roomId)
    }

    override suspend fun getMultiAllPhoto(
        roomId: Long,
        poseOrder: Int
    ): Response<List<MultiAllPhotoResponseDto>> {
        return yogaPoseHistoryApiService.getMultiAllPhoto(roomId, poseOrder)
    }
}