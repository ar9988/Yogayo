package com.d104.data.remote.api

import com.d104.data.remote.dto.BestPoseHistoryResponseDto
import com.d104.data.remote.dto.MultiAllPhotoResponseDto
import com.d104.data.remote.dto.MultiBestPhotoResponseDto
import com.d104.data.remote.dto.PoseRecordRequestDto
import com.d104.data.remote.dto.PoseRecordResponseDto
import com.d104.data.remote.dto.YogaPoseHistoryDetailResponseDto
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface YogaPoseHistoryApiService {

    @Multipart
    @POST("api/yoga/history/{poseId}")
    suspend fun postYogaPoseHistory(
        @Path("poseId") poseId: Long,
        @Part("poseRecordRequest") poseRecordRequestDto: PoseRecordRequestDto,
        @Part recordImg: MultipartBody.Part
    ): Response<PoseRecordResponseDto>

    @GET("api/yoga/history")
    suspend fun getYogaBestHistories(): Response<List<BestPoseHistoryResponseDto>>

    @GET("api/yoga/history/{poseId}")
    suspend fun getYogaPoseHistoryDetail(
        @Path("poseId") poseId: Long
    ): Response<YogaPoseHistoryDetailResponseDto>

    @GET("api/multi/{roomId}")
    suspend fun getMultiBestPhoto(@Path("roomId") roomId: Long): Response<List<MultiBestPhotoResponseDto>>

    @GET("api/multi/{roomId}/{poseOrder}")
    suspend fun getMultiAllPhoto(
        @Path("roomId") roomId: Long,
        @Path("roomOrderIndex") poseOrder: Int
    ): Response<List<MultiAllPhotoResponseDto>>

}