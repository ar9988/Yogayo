package com.d104.domain.model

data class PeerUser(
    val id: String,
    val nickName: String,
    val isReady: Boolean = false,
    val totalScore: Int = 0,
    val roundScore: Float = 0.0f
)