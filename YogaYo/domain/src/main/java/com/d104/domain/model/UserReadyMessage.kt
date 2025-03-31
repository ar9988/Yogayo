package com.d104.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("user_ready")
data class UserReadyMessage(
    val peerId: String,
    val isReady: Boolean,
    override val type: String
) : SignalingMessage()