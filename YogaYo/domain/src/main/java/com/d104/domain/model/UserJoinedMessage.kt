package com.d104.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("user_joined")
data class UserJoinedMessage(
    override val fromPeerId: String,
    val userNickName: String,
    override val type: String = "user_joined"
) : SignalingMessage()