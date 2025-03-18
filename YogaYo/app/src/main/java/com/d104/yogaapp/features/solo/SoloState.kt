package com.d104.yogaapp.features.solo

import com.d104.domain.model.UserCourse

data class SoloState(
    val courses: List<UserCourse> = emptyList(),
    val selectedCourseId: Long? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)