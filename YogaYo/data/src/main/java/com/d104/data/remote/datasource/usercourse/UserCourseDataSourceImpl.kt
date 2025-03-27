package com.d104.data.remote.datasource.usercourse

import com.d104.data.remote.api.UserCourseApiService
import com.d104.data.remote.api.YogaPoseApiService
import com.d104.data.remote.datasource.yogapose.YogaPoseDataSource
import com.d104.data.remote.dto.CourseRequestDto
import com.d104.data.remote.dto.CourseResponseDto
import com.d104.domain.model.UserCourse
import retrofit2.Response
import javax.inject.Inject

class UserCourseDataSourceImpl @Inject constructor(
    private val userCourseApiService: UserCourseApiService
): UserCourseDataSource {
    override suspend fun getUserCourses(): Response<List<CourseResponseDto>> = userCourseApiService.getUserCourses()

    override suspend fun createUserCourse(courseRequestBody: CourseRequestDto): Response<CourseResponseDto> = userCourseApiService.createUserCourse(courseRequestBody)

    override suspend fun updateUserCourse(
        courseId: Long,
        courseRequestBody: CourseRequestDto
    ): Response<CourseResponseDto> = userCourseApiService.updateUserCourse(courseId,courseRequestBody)

    override suspend fun deleteUserCourse(courseId: Long): Response<Boolean> = userCourseApiService.deleteUserCourse(courseId)
}