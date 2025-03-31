package com.d104.data.remote.api

import com.d104.data.remote.dto.PoseRecordRequest
import com.d104.data.remote.dto.PoseRecordResponse
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
        @Path("poseId") poseId:Long,
        @Part("poseRecordRequest") poseRecordRequest: PoseRecordRequest,
        @Part recordImg: MultipartBody.Part
    ): Response<PoseRecordResponse>

    @GET("api/yoga/history")
    suspend fun getYogaBestHistories():Response<List<PoseRecordResponse>>

    @GET("api/yoga/history/{poseId}")
    suspend fun getYogaPoseHistories():Response<List<PoseRecordResponse>>




}