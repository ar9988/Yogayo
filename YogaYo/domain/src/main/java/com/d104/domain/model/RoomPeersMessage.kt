package com.d104.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("room_peers")
data class RoomPeersMessage(
    val peerIds: List<String>, override val type: String, override val fromPeerId: String
) : SignalingMessage()