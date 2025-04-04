package com.d104.domain.model

import com.sun.jndi.toolkit.url.Uri

data class FinalPoseResult(
    val poseId:Long,
    val accuracy: Float,
    val time: Float,
    val imageUri: String? // 저장된 이미지의 URI
)