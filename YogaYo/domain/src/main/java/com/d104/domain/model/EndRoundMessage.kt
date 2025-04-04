package com.d104.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class EndRoundMessage(
    override val type: String = "round_end",
    override val fromPeerId: String
): SignalingMessage()