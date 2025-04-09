package com.d104.data.repository

import android.net.Uri
import android.util.Log
import com.d104.data.mapper.PoseRecordMapper
import com.d104.data.mapper.YogaPoseHistoryDetailMapper
import com.d104.data.remote.datasource.yogaposehistory.YogaPoseHistoryDataSource
import com.d104.data.remote.dto.PoseRecordRequestDto
import com.d104.domain.model.BestPoseRecord
import com.d104.domain.model.MultiBestPhoto
import com.d104.domain.model.MultiPhoto
import com.d104.domain.model.YogaPoseHistoryDetail
import com.d104.domain.model.YogaPoseRecord
import com.d104.domain.repository.YogaPoseHistoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException
import javax.inject.Inject

class YogaPoseHistoryRepositoryImpl @Inject constructor(
    private val yogaPoseHistoryDataSource: YogaPoseHistoryDataSource,
    private val poseRecordMapper: PoseRecordMapper,
    private val yogaPoseHistoryDetailMapper: YogaPoseHistoryDetailMapper

    ):YogaPoseHistoryRepository{
    override suspend fun postYogaPoseHistory(
        poseId: Long,
        roomRecordId: Long?,
        accuracy:Float,
        ranking: Int?,
        poseTime: Float,
        imgUri:String,
    ): Flow<Result<YogaPoseRecord>> = flow{
        try{
            val response = withContext(Dispatchers.IO){
                val recordImgPart: MultipartBody.Part? = if (imgUri.isNotBlank()) {
                    createMultipartBodyPartFromUri(imgUri, "recordImg")
                } else {
                    null
                }
                Log.d("PostYogaPoseHistory","img:$recordImgPart")
                yogaPoseHistoryDataSource.postYogaPoseHistory(
                    poseId = poseId,
                    poseRecordRequestDto = PoseRecordRequestDto(
                        roomRecordId,
                        accuracy,
                        ranking,
                        poseTime
                    ),
                    recordImg = recordImgPart ?: MultipartBody.Part.createFormData("recordImg", "", "".toRequestBody("text/plain".toMediaType()))
                )
            }
            val body = response.body()
            Log.d("PostYogaPoseHistory","body:$body")
            if(response.isSuccessful && body!=null){
                emit(Result.success(poseRecordMapper.toYogaPoseRecord(body)))
            } else {
                emit(Result.failure(IOException("API 호출 실패: ${response.code()} ${response.message()}")))
            }


        }catch (e:Exception){
            emit(Result.failure(e))
        }
    }


    override suspend fun getYogaBestHistories(): Flow<Result<List<BestPoseRecord>>>  = flow{
        try {
            val response = withContext(Dispatchers.IO){
                yogaPoseHistoryDataSource.getYogaBestHistories()

            }
            val body = response.body()
            if(response.isSuccessful && body != null){
                emit(Result.success(poseRecordMapper.toBestPoseRecordList(body)))
            } else {
                emit(Result.failure(IOException("API 호출 실패: ${response.code()} ${response.message()}")))
            }
        }catch (e:Exception){
            emit(Result.failure(e))
        }
    }

    override suspend fun getYogaPoseHistoryDetail(poseId:Long): Flow<Result<YogaPoseHistoryDetail>> = flow{
        try {
            val response = withContext(Dispatchers.IO) {
                yogaPoseHistoryDataSource.getYogaPoseHistoryDetail(poseId)
            }
            val body = response.body()
            if (response.isSuccessful && body != null) {
                emit(Result.success(yogaPoseHistoryDetailMapper.mapToDomain(body)))
            } else {
                emit(Result.failure(IOException("API 호출 실패: ${response.code()} ${response.message()}")))
            }
        }catch (e:Exception){
            emit(Result.failure(e))
        }
    }

    override suspend fun getMultiBestPhoto(roomId: Long): Flow<Result<List<MultiBestPhoto>>> {
        try {
            val response = withContext(Dispatchers.IO) {
                yogaPoseHistoryDataSource.getMultiBestPhoto(roomId)
            }
            val body = response.body()
            return if (response.isSuccessful && body != null) {
                flow { emit(Result.success(yogaPoseHistoryDetailMapper.mapToDomainList(body))) }
            } else {
                flow { emit(Result.failure(IOException("API 호출 실패: ${response.code()} ${response.message()}"))) }
            }
        } catch (e: Exception) {
            return flow { emit(Result.failure(e)) }
        }
    }

    override suspend fun getMultiAllPhoto(
        roomId: Long,
        poseIndex: Int
    ): Flow<Result<List<MultiPhoto>>> {
        return try {
            val response = withContext(Dispatchers.IO) {
                yogaPoseHistoryDataSource.getMultiAllPhoto(roomId, poseIndex)
            }
            val body = response.body()
            if (response.isSuccessful && body != null) {
                flow { emit(Result.success(yogaPoseHistoryDetailMapper.mapToDomainList2(body))) }
            } else {
                flow { emit(Result.failure(IOException("API 호출 실패: ${response.code()} ${response.message()}"))) }
            }
        } catch (e: Exception) {
            flow { emit(Result.failure(e)) }
        }
    }

    fun createMultipartBodyPartFromUri(
        fileUri: String,
        paramName: String
    ): MultipartBody.Part? {
        return try {
            // file:/// URI를 처리
            val uri = Uri.parse(fileUri)

            // 파일 경로가 file:/// 형식인 경우 직접 File 객체 생성
            val file = if (uri.scheme == "file") {
                File(uri.path ?: throw IOException("Invalid file path"))
            } else {
                // content:// URI인 경우 다른 처리가 필요할 수 있음
                throw IOException("Unsupported URI scheme: ${uri.scheme}")
            }

            if (!file.exists()) {
                throw IOException("File does not exist: $fileUri")
            }

            // 파일 이름 추출
            val fileName = file.name

            // 파일 타입 추정
            val mimeType = when {
                fileName.endsWith(".jpg", ignoreCase = true) ||
                        fileName.endsWith(".jpeg", ignoreCase = true) -> "image/jpeg"
                fileName.endsWith(".png", ignoreCase = true) -> "image/png"
                fileName.endsWith(".gif", ignoreCase = true) -> "image/gif"
                else -> "application/octet-stream" // 기본 타입
            }

            // RequestBody 생성
            val requestFile = file.asRequestBody(mimeType.toMediaTypeOrNull())

            // MultipartBody.Part 생성
            MultipartBody.Part.createFormData(paramName, fileName, requestFile)

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}