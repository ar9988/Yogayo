package com.d104.domain.repository

import com.d104.domain.model.UserCourse
import com.d104.domain.model.YogaPoseWithOrder
import kotlinx.coroutines.flow.Flow

interface UserCourseRepository {
    suspend fun getYogaCourses() : Flow<Result<List<UserCourse>>>
    suspend fun createYogaCourse(courseName:String, poses:List<YogaPoseWithOrder>): Flow<Result<UserCourse>>
    suspend fun updateYogaCourse(courseId:Long,courseName:String,poses:List<YogaPoseWithOrder>,isTutorial:Boolean=true): Flow<Result<UserCourse>>
    suspend fun deleteYogaCourse(courseId:Long): Flow<Result<Boolean>>

}