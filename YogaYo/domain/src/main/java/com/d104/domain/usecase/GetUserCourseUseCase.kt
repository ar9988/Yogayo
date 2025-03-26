package com.d104.domain.usecase

import com.d104.domain.model.UserCourse
import com.d104.domain.repository.UserCourseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserCourseUseCase @Inject constructor(
    private val userCourseRepository: UserCourseRepository
){
    suspend operator fun invoke(): Flow<Result<List<UserCourse>>>{
        return userCourseRepository.getYogaCourses()
    }
}