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
            // 빈 줄 없이 헤더만 있는 경우 대비, 인덱스 계속 업데이트
            bodyStartIndex = i + 1
        }

        val body = if (bodyStartIndex < lines.size) {
            lines.subList(bodyStartIndex, lines.size).joinToString("\n")
            // body 끝의 NULL 문자는 파싱 시 제거 (보낼 때는 프레임 끝에만 붙음)
            // .removeSuffix(NULL.toString())
        } else {
            ""
        }

        // 파싱 시 body 끝의 NULL 문자 제거 (프레임 자체 끝 NULL은 제외)
        val finalBody = body.removeSuffix(NULL.toString())

        return Triple(command, headers, finalBody)
    }

    /**
     * CONNECT 프레임을 생성합니다.
     * @param host 서버 호스트 주소
     * @param token 인증을 위한 JWT 토큰 (Bearer 접두사 제외한 순수 토큰 값)
     * @param acceptVersion 사용할 STOMP 버전 (기본값: 1.2)
     * @return 생성된 CONNECT 프레임 문자열
     */
    fun buildConnectFrame(host: String, token: String): String {
        return """
        CONNECT
        accept-version:1.2
        host:$host
        Authorization:Bearer $token
        heart-beat:10000,10000

        ${NULL}
    """.trimIndent().replace("\n", "\n")
    }

    // --- 중요: 서버가 'Authorization' 대신 'token' 헤더를 직접 받는 경우 아래 함수 사용 ---
    /*
    fun buildConnectFrameWithTokenHeader(host: String, token: String, acceptVersion: String = V1_2): String {
        // 'token' 헤더 직접 추가
        val tokenHeader = "token: $token"

        return """
        CONNECT
        accept-version:$acceptVersion
        host:$host
        $tokenHeader
        heart-beat:10000,10000

        $NULL
        """.trimIndent()
    }
    */


    // SUBSCRIBE 프레임 생성
    fun buildSubscribeFrame(destination: String, id: String): String {
        val builder = StringBuilder()
        builder.append("SUBSCRIBE\n") // 명령어 시작 - 앞에 아무것도 없는지 확인
        builder.append("id:$id\n")
        builder.append("destination:$destination\n")
        builder.append("ack:auto\n")
        builder.append("\n")          // 헤더-바디 구분 빈 줄
        builder.append(NULL)     // 실제 Null 문자 추가

        return builder.toString()
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
        // 현재는 그대로 전송
        val contentLength = body.toByteArray(Charsets.UTF_8).size // content-length 추가 (선택적)

        return """
         SEND
         destination:$destination
         content-type:application/json;charset=UTF-8
         content-length:$contentLength

         $body$NULL
         """.trimIndent() // content-type, content-length는 예시
    }
}