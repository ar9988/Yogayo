package com.d104.data.repository

import com.d104.data.mapper.UserCourseMapper
import com.d104.data.mapper.YogaPoseMapper
import com.d104.data.remote.datasource.usercourse.UserCourseDataSource
import com.d104.data.remote.datasource.yogapose.YogaPoseDataSource
import com.d104.data.remote.dto.CourseRequestDto
import com.d104.domain.model.UserCourse
import com.d104.domain.model.YogaPoseWithOrder
import com.d104.domain.repository.UserCourseRepository
import com.d104.domain.repository.YogaPoseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

class UserCourseRepositoryImpl @Inject constructor(
    private val userCourseDataSource: UserCourseDataSource,
    private val userCourseMapper:UserCourseMapper
): UserCourseRepository {
    override suspend fun getYogaCourses(): Flow<Result<List<UserCourse>>>  = flow{
        try{
            val response = withContext(Dispatchers.IO){
                userCourseDataSource.getUserCourses()
            }
            val body = response.body()
            if (response.isSuccessful && body != null) {
                emit(Result.success(userCourseMapper.mapResponsesToDomain(body)))
            } else {
                emit(Result.failure(IOException("API 호출 실패: ${response.code()} ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override suspend fun createYogaCourse(
        courseName: String,
        poses: List<YogaPoseWithOrder>
    ): Flow<Result<UserCourse>>  = flow{
        try{
            val response = withContext(Dispatchers.IO){
                userCourseDataSource.createUserCourse(CourseRequestDto(courseName,poses))
            }
            val body = response.body()
            if (response.isSuccessful && body != null) {
                emit(Result.success(userCourseMapper.mapResponseToDomain(body)))
            } else {
                emit(Result.failure(IOException("API 호출 실패: ${response.code()} ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override suspend fun updateYogaCourse(
        courseId: Long,
        courseName: String,
        poses: List<YogaPoseWithOrder>,
        isTutorial:Boolean
    ): Flow<Result<UserCourse>> = flow{
        try{
            val response = withContext(Dispatchers.IO){
                userCourseDataSource.updateUserCourse(courseId,CourseRequestDto(courseName,poses,isTutorial))
            }
            val body = response.body()
            if (response.isSuccessful && body != null) {
                emit(Result.success(userCourseMapper.mapResponseToDomain(body)))
            } else {
                emit(Result.failure(IOException("API 호출 실패: ${response.code()} ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override suspend fun deleteYogaCourse(courseId: Long): Flow<Result<Boolean>> =flow{
        try{
            val response = withContext(Dispatchers.IO){
                userCourseDataSource.deleteUserCourse(courseId)
            }
            val body = response.body()
            if (response.isSuccessful && body != null) {
                emit(Result.success(body))
            } else {
                emit(Result.failure(IOException("API 호출 실패: ${response.code()} ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}