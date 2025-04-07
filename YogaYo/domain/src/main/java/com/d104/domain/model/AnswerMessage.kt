package com.d104.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class AnswerMessage(
    override val type: String = "answer", // 고정값 또는 생성 시 할당
    override val fromPeerId: String,
    val toPeerId: String,
    val sdp: String
) : SignalingMessage()