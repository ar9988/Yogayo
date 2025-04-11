package com.d104.domain.model



data class CapturedPoseImage(
    val poseId: String,
    val imageUri: String,
    val accuracy: Float
)