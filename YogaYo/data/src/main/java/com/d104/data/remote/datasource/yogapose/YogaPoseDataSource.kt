package com.d104.data.remote.datasource.yogapose

import com.d104.data.remote.dto.YogaPoseDto
import retrofit2.Response

interface YogaPoseDataSource {
    suspend fun getYogaPoses(): Response<List<YogaPoseDto>>
    suspend fun getYogaPoseDetail(yogaId:Long): Response<YogaPoseDto>
}