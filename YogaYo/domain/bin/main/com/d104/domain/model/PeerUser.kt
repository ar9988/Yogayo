package com.d104.domain.model

data class PeerUser(
    val nickName: String,
    val readyState: Boolean,
    val totalScore: Int,
    val roundScore: Float = 0.0f
)