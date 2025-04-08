// com/d104/data/remote/utils/StompUtils.kt
package com.d104.data.remote.utils

import android.util.Log

object StompUtils {

    private const val NULL = "\u0000" // Use Unicode null character directly
    private const val EOL = "\n"      // Define newline for clarity

    // Reverted buildConnectFrame using raw string
    fun buildConnectFrame(host: String, token: String, outgoingHeartbeat: Long = 300000, incomingHeartbeat: Long = 300000): String {
        val headers = mutableMapOf(
            "accept-version" to "1.2",
            "host" to host,
            "Authorization" to "Bearer $token"
        )

        // 하트비트 헤더 조건부 추가
        if (outgoingHeartbeat > 0 || incomingHeartbeat > 0) {
            headers["heart-beat"] = "$outgoingHeartbeat,$incomingHeartbeat"
        }

        val frame = buildFrame("CONNECT", headers)

        Log.v("StompUtils", "Building Frame: CONNECT (secured details)")
        return frame
    }

    // Reverted buildSubscribeFrame using raw string
    fun buildSubscribeFrame(destination: String, id: String): String {
        val headers = mapOf(
            "destination" to destination,
            "id" to id,
            "ack" to "auto"
        )
        return buildFrame("SUBSCRIBE", headers).also {
            Log.v("StompUtils", "SUBSCRIBE Frame: ${it.replace(NULL, "\\0")}")
        }
    }

    fun buildSendFrame(destination: String, body: String): String {
        // 헤더를 맵 형태로 정의
        val headers = mapOf(
            "destination" to destination,
            "content-type" to "application/json;charset=UTF-8" // JSON 형식 사용
        )

        return buildFrame("SEND", headers, body)
    }

    private fun buildFrame(
        command: String,
        headers: Map<String, String> = emptyMap(),
        body: String = ""
    ): String {
        Log.d("StompUtils", "Building Frame: $command with headers: $headers and body: $body")
        return StringBuilder().apply {
            append("$command\n")
            headers.forEach { (k, v) -> append("$k:$v\n") }
            append("\n") // 헤더-바디 구분
            append(body)
            append(NULL)
        }.toString()
    }
    // Reverted buildDisconnectFrame using raw string
    fun buildDisconnectFrame(receiptId: String? = null): String {
        val receiptHeader = receiptId?.let { "receipt:$it$EOL" } ?: ""

        val frame = """
            DISCONNECT
            $receiptHeader
            $NULL
            """.trimIndent()

        Log.v("StompUtils", "Building Frame: ${frame.replace(NULL, "\\0").take(300)}...")
        return frame
    }

    // buildFrame helper is no longer needed with this approach

    // parseFrame remains the same as it deals with incoming frames
    fun parseFrame(frameText: String): Triple<String, Map<String, String>, String> {
        try {
            // Handle potential leading/trailing newlines before splitting
            val trimmedFrame = frameText.trim { it <= ' ' || it == '\u0000' }
            // Split headers from body
            val parts = trimmedFrame.split("$EOL$EOL", limit = 2)
            if (parts.isEmpty()) {
                throw IllegalArgumentException("Empty frame received")
            }

            val headerLines = parts[0].split(EOL)
            if (headerLines.isEmpty()) {
                throw IllegalArgumentException("Frame without command received")
            }

            val command = headerLines[0].trim()
            val headers = mutableMapOf<String, String>()
            for (i in 1 until headerLines.size) {
                val headerLine = headerLines[i]
                // Allow for empty header lines, skip them
                if (headerLine.isBlank()) continue
                val headerParts = headerLine.split(":", limit = 2)
                if (headerParts.size == 2) {
                    // Trim both key and value
                    headers[headerParts[0].trim()] = headerParts[1].trim()
                } else {
                    Log.w("StompUtils", "Malformed header line ignored: '$headerLine'")
                }
            }

            // The body is everything after the double newline, up to the NULL char
            val bodyWithNull = if (parts.size > 1) parts[1] else ""
            // Find the first NULL character to correctly handle frames with embedded NULLs (though unusual)
            val nullCharIndex = bodyWithNull.indexOf(NULL)
            val body = if(nullCharIndex != -1) {
                bodyWithNull.substring(0, nullCharIndex)
            } else {
                // If somehow NULL is missing (non-compliant frame), take the whole part
                Log.w("StompUtils", "Received frame part without trailing NULL: ${bodyWithNull.take(50)}...")
                bodyWithNull
            }
            Log.d("StompRepo","body is $body")

            return Triple(command, headers, body)
        } catch (e: Exception) {
            Log.e("StompUtils", "Failed to parse STOMP frame: ${frameText.take(100)}...", e)
            // Return a dummy ERROR frame representation or re-throw
            return Triple("PARSE_ERROR", mapOf("message" to "Frame parsing error", "original_frame_snippet" to frameText.take(100)), e.message ?: "Parsing failed")
        }
    }
}