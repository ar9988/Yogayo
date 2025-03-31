package com.d104.domain.model

data class Badge (
    val badgeId :Long,
    val badgeName:String,
    val badgeProgress : Int,
    val highLevel: Int,
    val badgeDetails: List<BadgeDetail>

)