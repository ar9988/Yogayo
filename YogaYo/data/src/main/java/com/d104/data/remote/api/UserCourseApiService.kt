package com.d104.data.remote.api

import com.d104.data.remote.dto.CourseRequestDto
import com.d104.data.remote.dto.CourseResponseDto
import com.d104.domain.model.UserCourse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface UserCourseApiService {
    @GET("api/yoga/course")
    suspend fun getUserCourses(): Response<List<CourseResponseDto>>

    @POST("api/yoga/course")
    suspend fun createUserCourse(
        @Body courseRequestBody: CourseRequestDto
    ): Response<CourseResponseDto>

    @PUT("api/yoga/course/{courseId}")
    suspend fun updateUserCourse(
        @Path("courseId") courseId:Long,
        @Body courseRequestBody: CourseRequestDto
    ): Response<CourseResponseDto>

    @DELETE("api/yoga/course/{courseId}")
    suspend fun deleteUserCourse(
        @Path("courseId") courseId:Long,
    ): Response<Boolean>
}