package com.d104.domain.model

import kotlinx.serialization.Serializable

@Serializable
sealed class DataChannelMessage { // WebRTC 용 부모 클래스
}