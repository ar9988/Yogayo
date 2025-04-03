package com.d104.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class IceCandidateData(
    val sdpMid: String,         // candidate.sdpMid
    val sdpMLineIndex: Int,   // candidate.sdpMLineIndex
    val sdpCandidate: String    // candidate.sdp
)