package com.d104.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class GameStateMessage (
    val state: Int,
    override val type: String = "game_state"
):SignalingMessage()