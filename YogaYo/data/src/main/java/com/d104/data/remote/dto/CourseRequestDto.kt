package com.d104.data.remote.dto

import com.d104.domain.model.YogaPoseWithOrder

data class CourseRequestDto(
    val courseName:String,
    val poses: List<YogaPoseWithOrder>,
    val tutorial:Boolean = true
)
