package com.d104.data.mapper

import com.d104.data.remote.dto.CourseResponseDto
import com.d104.domain.model.UserCourse
import javax.inject.Inject

class UserCourseMapper @Inject constructor(
    private val poseMapper: YogaPoseMapper
) {

    /**
     * API 응답으로부터 받은 코스 목록을 도메인 모델 리스트로 변환합니다.
     */
    fun mapResponsesToDomain(responses: List<CourseResponseDto>): List<UserCourse> {
        return responses.map { mapResponseToDomain(it) }
    }

    /**
     * 단일 코스 응답을 도메인 모델로 변환합니다.
     */
    fun mapResponseToDomain(response: CourseResponseDto): UserCourse {
        return UserCourse(
            courseId = response.userCourseId.toLong(),
            courseName = response.courseName,
            tutorial = response.tutorial,
            poses = response.poses.map { poseMapper.mapToDomain(it) }
        )
    }


}