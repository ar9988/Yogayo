package com.d104.domain.usecase

import com.d104.domain.model.UserCourse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetCourseUseCase @Inject constructor(){
    operator fun invoke(): Flow<List<UserCourse>> = flow {  emptyList<UserCourse>() }
}