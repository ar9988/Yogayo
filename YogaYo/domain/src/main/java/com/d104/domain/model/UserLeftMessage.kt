package com.d104.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("user_left")
data class UserLeftMessage(
    val peerId: String,
    override val type: String = "user_left"
) : SignalingMessage()