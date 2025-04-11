package com.d104.data.repository

import android.util.Log
import com.d104.data.mapper.YogaPoseMapper
import com.d104.data.remote.datasource.yogapose.YogaPoseDataSource
import com.d104.domain.model.YogaPose
import com.d104.domain.repository.YogaPoseRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

class YogaPoseRepositoryImpl @Inject constructor(
    private val yogaPoseDataSource: YogaPoseDataSource,
    private val yogaPoseMapper: YogaPoseMapper
):YogaPoseRepository{
    override suspend fun getYogaPoses(): Flow<Result<List<YogaPose>>> = flow {
        try {
            val response = withContext(Dispatchers.IO) {
                yogaPoseDataSource.getYogaPoses()
            }

            val body = response.body()
            Log.d("YogaPoseRepositoryImpl", "API 호출 성공: $body")
            if (response.isSuccessful && body != null) {
                emit(Result.success(yogaPoseMapper.mapToDomainList(body)))
            } else {
                emit(Result.failure(IOException("API 호출 실패: ${response.code()} ${response.message()}")))
            }
        } catch (e: Exception) {
            Log.e("YogaPoseRepositoryImpl", "API 호출 실패", e)
            emit(Result.failure(e))
        }
    }

    override suspend fun getYogaPoseDetail(poseId: Long): Flow<Result<YogaPose>> = flow {
        try {
            val response = withContext(Dispatchers.IO) {
                yogaPoseDataSource.getYogaPoseDetail(poseId)
            }

            val body = response.body()
            if (response.isSuccessful && body != null) {
                emit(Result.success(yogaPoseMapper.mapToDomain(body)))
            } else {
                emit(Result.failure(IOException("API 호출 실패: ${response.code()} ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}