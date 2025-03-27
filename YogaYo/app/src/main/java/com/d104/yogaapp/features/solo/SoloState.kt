package com.d104.yogaapp.features.solo

import com.d104.domain.model.UserCourse
import com.d104.domain.model.YogaPose

data class SoloState(
    val courses: List<UserCourse> = emptyList(),
    val yogaPoses:List<YogaPose> = emptyList(),
    val selectedCourseId: Long? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val showAddCourseDialog: Boolean = false,
    val yogaPoseLoading : Boolean = false,
    val isLogin:Boolean = false
)