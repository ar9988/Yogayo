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

    /**
     * Builds a SEND frame.
     * The provided 'body' string will be wrapped inside a JSON object under the key "payload".
     * e.g., if body is '{"type":"user_joined", "data":"..."}', the actual frame body will be
     * '{"payload":{"type":"user_joined", "data":"..."}}'
     *
     * @param destination The STOMP destination.
     * @param body The JSON string representing the inner payload content.
     * @return The complete STOMP SEND frame as a String.
     */
    fun buildSendFrame(destination: String, body: String): String {
        // Define headers
        val headers = mapOf(
            "destination" to destination,
            "content-type" to "application/json;charset=UTF-8" // JSON format
        )

        // Wrap the original body string inside a "payload" JSON object
        // IMPORTANT: Assumes the input 'body' is already a valid JSON value representation (object, array, string, number, etc.)
        val wrappedBody = """{"payload":$body}"""

        Log.d("StompUtils", "Original body for SEND: $body")
        Log.d("StompUtils", "Wrapped body for SEND: $wrappedBody")

        // Build the frame using the wrapped body
        return buildFrame("SEND", headers, wrappedBody)
    }

    private fun buildFrame(
        command: String,
        headers: Map<String, String> = emptyMap(),
        body: String = ""
    ): String {
        // Keep the detailed log here for debugging frame construction
        Log.d("StompUtils", "Building Frame: Command='$command', Headers='$headers', Body (first 100 chars)='${body.take(100)}'")
        return StringBuilder().apply {
            append(command)
            append(EOL) // Use constant
            headers.forEach { (k, v) -> append("$k:$v").append(EOL) } // Use constant
            append(EOL) // Header-body separator
            append(body)
            append(NULL) // Use constant
        }.toString()
    }
    // Reverted buildDisconnectFrame using raw string
    fun buildDisconnectFrame(receiptId: String? = null): String {
        val headers = mutableMapOf<String, String>()
        receiptId?.let { headers["receipt"] = it }

        return buildFrame("DISCONNECT", headers).also {
            Log.v("StompUtils", "Building Frame: DISCONNECT (receipt: $receiptId)")
        }
    }


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
            // Log removed from here as it was specific to StompRepo

            return Triple(command, headers, body)
        } catch (e: Exception) {
            Log.e("StompUtils", "Failed to parse STOMP frame: ${frameText.take(100)}...", e)
            // Return a dummy ERROR frame representation or re-throw
            return Triple("PARSE_ERROR", mapOf("message" to "Frame parsing error", "original_frame_snippet" to frameText.take(100)), e.message ?: "Parsing failed")
        }
    }
}