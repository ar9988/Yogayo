package com.d104.data.remote.datasource.usercourse

import com.d104.data.remote.dto.CourseRequestDto
import com.d104.data.remote.dto.CourseResponseDto
import com.d104.data.remote.dto.YogaPoseDto
import com.d104.domain.model.UserCourse
import retrofit2.Response

interface UserCourseDataSource {
    suspend fun getUserCourses(): Response<List<CourseResponseDto>>
    suspend fun createUserCourse(courseRequestBody:CourseRequestDto): Response<CourseResponseDto>
    suspend fun updateUserCourse(courseId:Long, courseRequestBody:CourseRequestDto) : Response<CourseResponseDto>
    suspend fun deleteUserCourse(courseId:Long):Response<Boolean>
}