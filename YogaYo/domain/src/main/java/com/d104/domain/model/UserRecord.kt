package com.d104.domain.model

data class UserRecord(
    val userId:Long,
    val userName:String,
    val userNickName:String,
    val userProfile:String,
    val exDays:Int,
    val exConDays:Int,
    val roomWin:Int
)
