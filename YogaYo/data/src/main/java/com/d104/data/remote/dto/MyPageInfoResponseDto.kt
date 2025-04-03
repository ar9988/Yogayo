package com.d104.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MyPageInfoResponseDto(
    @Json(name = "userId")
    val userId:Long,

    @Json(name = "userName")
    val userName:String,

    @Json(name = "userNickName")
    val userNickName:String,

    @Json(name = "userProfile")
    val userProfile:String?,

    @Json(name = "exDays")
    val exDays:Int?,

    @Json(name = "exConDays")
    val exConDays:Int?,

    @Json(name = "roomWin")
    val roomWin:Int?
)
