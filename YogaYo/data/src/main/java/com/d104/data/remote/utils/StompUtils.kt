package com.d104.data.remote.utils

// STOMP 프레임 파싱 및 생성을 위한 간단한 헬퍼 클래스/함수
object StompUtils {
    const val V1_2 = "1.2" // 사용할 STOMP 버전
    const val NULL = '\u0000' // 프레임 종료 문자

    // 간단한 프레임 파서 (헤더와 바디 분리)
    fun parseFrame(rawFrame: String): Triple<String, Map<String, String>, String> {
        val lines = rawFrame.lines()
        if (lines.isEmpty()) throw IllegalArgumentException("Empty STOMP frame")

        val command = lines[0]
        val headers = mutableMapOf<String, String>()
        var bodyStartIndex = 1 // 헤더 다음 줄부터 바디 시작 가능성

        for (i in 1 until lines.size) {
            val line = lines[i]
            if (line.isBlank()) { // 헤더와 바디 구분 빈 줄
                bodyStartIndex = i + 1
                break
            }
            val parts = line.split(":", limit = 2)
            if (parts.size == 2) {
                headers[parts[0].trim()] = parts[1].trim()
            }
            bodyStartIndex = i + 1 // 헤더만 있고 바디 없는 경우 대비
        }

        val body = if (bodyStartIndex < lines.size) {
            lines.subList(bodyStartIndex, lines.size).joinToString("\n")
                .removeSuffix(NULL.toString()) // 끝의 NULL 문자 제거
        } else {
            ""
        }

        return Triple(command, headers, body)
    }

    // CONNECT 프레임 생성
    fun buildConnectFrame(host: String, acceptVersion: String = V1_2): String {
        return """
        CONNECT
        accept-version:$acceptVersion
        host:$host
        heart-beat:10000,10000

        $NULL
        """.trimIndent() // heart-beat는 선택 사항 (서버 지원 확인 필요)
    }

    // SUBSCRIBE 프레임 생성
    fun buildSubscribeFrame(destination: String, id: String): String {
        return """
        SUBSCRIBE
        id:$id
        destination:$destination
        ack:auto

        $NULL
        """.trimIndent() // ack 모드는 auto, client, client-individual 중 선택
    }

    // UNSUBSCRIBE 프레임 생성
    fun buildUnsubscribeFrame(id: String): String {
        return """
        UNSUBSCRIBE
        id:$id

        $NULL
        """.trimIndent()
    }

    // DISCONNECT 프레임 생성
    fun buildDisconnectFrame(receiptId: String? = null): String {
        val receiptHeader = if (receiptId != null) "receipt:$receiptId\n" else ""
        return """
        DISCONNECT
        $receiptHeader
        $NULL
        """.trimIndent()
    }

    // SEND 프레임 생성
    fun buildSendFrame(destination: String, body: String): String {
        // body에 NULL 문자가 있으면 STOMP 스펙에 따라 이스케이프 필요할 수 있음
        return """
         SEND
         destination:$destination
         content-type:application/json;charset=UTF-8

         $body$NULL
         """.trimIndent() // content-type은 예시
    }
}