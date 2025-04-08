package com.d104.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("score_update") // JSON 직렬화 시 type 필드 값으로 사용될 수 있음
data class ScoreUpdateMessage(
    val score: Float,
    val time: Float,
) : DataChannelMessage()