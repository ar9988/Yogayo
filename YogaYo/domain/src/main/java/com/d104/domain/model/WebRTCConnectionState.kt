package com.d104.domain.model

enum class WebRTCConnectionState {
    NEW,            // 초기 상태
    CONNECTING,     // 연결 시도 중
    CONNECTED,      // P2P 연결 성공 (DataChannel 사용 가능)
    DISCONNECTED,   // 연결 끊김 (일시적일 수 있음)
    FAILED,         // 연결 실패
    CLOSED          // 연결 완전 종료
}