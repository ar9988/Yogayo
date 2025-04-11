package com.d104.data.remote.api

import com.d104.data.remote.dto.YogaPoseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface YogaPoseApiService {
    @GET("api/yoga/all")
    suspend fun getYogaPoses(): Response<List<YogaPoseDto>>

    @GET("api/yoga/detail/{poseId}")
    suspend fun getYogaPoseDetail(
        @Path("poseId") courseId:Long
    ):Response<YogaPoseDto>
}