package com.d104.domain.repository

import com.d104.domain.model.YogaPose
import kotlinx.coroutines.flow.Flow

interface YogaPoseRepository {
    suspend fun getYogaPoses() : Flow<Result<List<YogaPose>>>
    suspend fun getYogaPoseDetail(poseId:Long) : Flow<Result<YogaPose>>



}