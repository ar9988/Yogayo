package com.d104.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class StartRoundMessage(
    override val type: String = "round_start"
): SignalingMessage()