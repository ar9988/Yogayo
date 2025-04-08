package com.d104.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class RequestPhotoMessage(
    override val type: String = "request_photo",
    override val fromPeerId: String,
    val toPeerId: String
):SignalingMessage()