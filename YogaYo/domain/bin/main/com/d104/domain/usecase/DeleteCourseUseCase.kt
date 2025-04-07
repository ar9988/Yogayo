package com.d104.domain.usecase

import com.d104.domain.repository.UserCourseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class DeleteCourseUseCase @Inject constructor(
    private val userCourseRepository: UserCourseRepository
){
    suspend operator fun invoke(courseId:Long): Flow<Result<Boolean>>{
        return userCourseRepository.deleteYogaCourse(courseId)
    }
}