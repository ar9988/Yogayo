package com.d104.data.remote.datasource.yogapose

import com.d104.data.remote.api.YogaPoseApiService
import com.d104.data.remote.dto.YogaPoseDto
import retrofit2.Response
import javax.inject.Inject

class YogaPoseDataSourceImpl @Inject constructor(
    private val yogaPoseApiService:YogaPoseApiService
): YogaPoseDataSource{
    override suspend fun getYogaPoses(): Response<List<YogaPoseDto>> = yogaPoseApiService.getYogaPoses()

    override suspend fun getYogaPoseDetail(yogaId:Long): Response<YogaPoseDto> = yogaPoseApiService.getYogaPoseDetail(yogaId)
}