package com.d104.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class DeleteCourseUseCase @Inject constructor(){
    operator fun invoke(): Flow<String?> = flow {  }
}