package com.d104.yogaapp.features.solo

import android.graphics.Bitmap
import com.d104.domain.model.UserCourse
import com.d104.domain.model.YogaPoseWithOrder

sealed class SoloIntent {
    object LoadCourses : SoloIntent()
    object ShowAddCourseDialog : SoloIntent()
    object HideAddCourseDialog : SoloIntent()
    data class CreateCourse(val courseName: String, val poses: List<YogaPoseWithOrder>) : SoloIntent()
    data class UpdateCourse(val courseId:Long,val courseName: String, val poses: List<YogaPoseWithOrder>):SoloIntent()
    data class DeleteCourse(val courseId:Long):SoloIntent()
    data class UpdateCourseTutorial(val course: UserCourse, val tutorial: Boolean) : SoloIntent()

}