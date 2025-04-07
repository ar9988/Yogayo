package com.d104.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("ice_candidate")
data class IceCandidateMessage(
    override val fromPeerId: String,
    val toPeerId: String,
    val candidate: IceCandidateData,
    override val type: String
) : SignalingMessage()