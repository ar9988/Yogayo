package com.d104.data.remote.dto

import com.d104.domain.model.YogaPose
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class CourseResponseDto (

    @Json(name = "userCourseId")
    val userCourseId : Long,


    @Json(name = "courseName")
    val courseName: String,

    @Json(name = "tutorial")
    val tutorial: Boolean,

    @Json(name = "createdAt")
    val createdAt:Long,

    @Json(name = "modifyAt")
    val modifyAt:Long?,

    @Json(name="poses")
    val poses:List<YogaPoseDto>

)