package com.d104.domain.utils

import com.d104.domain.model.DataChannelMessage
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun DataChannelMessage.toByteArray(json:Json): ByteArray {
    val jsonString = json.encodeToString(this)
    return jsonString.encodeToByteArray()
}