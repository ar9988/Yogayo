package com.d104.domain.model

data class UserCourse(
    val courseId: Long,
    val courseName: String,
    val tutorial: Boolean,
    val poses: List<YogaPose>
)