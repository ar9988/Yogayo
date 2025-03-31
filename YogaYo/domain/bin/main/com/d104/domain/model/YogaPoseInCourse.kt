package com.d104.domain.model

data class YogaPoseInCourse (
    val uniqueID: String,
    val pose : YogaPose,
    val order: Int = 0
)