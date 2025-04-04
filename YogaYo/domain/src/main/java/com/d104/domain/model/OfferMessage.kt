package com.d104.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("offer") // JSON 직렬화 시 type 필드 대신 사용 가능 (선택적)
data class OfferMessage(
    override val fromPeerId: String, // 누가 보냈는지
    val toPeerId: String,   // 누구에게 보내는지 (서버가 라우팅 해주면 생략 가능)
    val sdp: String,
    override val type: String
) : SignalingMessage()