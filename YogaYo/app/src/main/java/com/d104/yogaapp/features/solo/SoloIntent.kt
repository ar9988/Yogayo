package com.d104.yogaapp.features.solo

import com.d104.domain.model.YogaPoseWithOrder

sealed class SoloIntent {
    object LoadCourses : SoloIntent()
    data class CreateCourse(val courseName: String, val poses: List<YogaPoseWithOrder>) : SoloIntent()
    data class UpdateCourse(val courseId:Long,val courseName: String, val poses: List<YogaPoseWithOrder>):SoloIntent()
    data class DeleteCourse(val courseId:Long):SoloIntent()

}