package com.d104.domain.usecase

import com.d104.domain.model.UserCourse
import com.d104.domain.model.YogaPoseInCourse
import com.d104.domain.model.YogaPoseWithOrder
import com.d104.domain.repository.UserCourseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class CreateCourseUseCase @Inject constructor(
    private val userCourseRepository: UserCourseRepository
){
    suspend operator fun invoke(courseName:String,poses:List<YogaPoseWithOrder>): Flow<Result<UserCourse>> {
        return userCourseRepository.createYogaCourse(courseName,poses)
    }
}