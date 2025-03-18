package com.d104.domain.usecase

import com.d104.domain.model.YogaPoseInCourse
import com.d104.domain.model.YogaPoseWithOrder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class CreateCourseUseCase @Inject constructor(){
    operator fun invoke(courseName:String,poses:List<YogaPoseWithOrder>): Flow<String?> = flow {  }
}