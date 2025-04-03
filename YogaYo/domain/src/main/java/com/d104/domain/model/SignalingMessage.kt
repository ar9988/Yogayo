package com.d104.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class SignalingMessage {
    // 메시지 타입을 명시적으로 포함하는 것이 파싱에 유리할 수 있음
    abstract val type: String
}