package com.d104.domain.usecase

import com.d104.domain.model.UserCourse
import com.d104.domain.model.YogaPoseWithOrder
import com.d104.domain.repository.UserCourseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class UpdateCourseUseCase @Inject constructor(
    private val userCourseRepository: UserCourseRepository
){
    suspend operator fun invoke(courseId:Long,courseName:String,poses:List<YogaPoseWithOrder>,isTutorial:Boolean=true): Flow<Result<UserCourse>> {
        return userCourseRepository.updateYogaCourse(courseId,courseName,poses,isTutorial)
    }
}