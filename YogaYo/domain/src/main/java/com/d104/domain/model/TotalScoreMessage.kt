package com.d104.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class TotalScoreMessage(
    val score: Int,
    val toPeerId: String,
    override val type: String = "total_score",
    override val fromPeerId: String
):SignalingMessage()