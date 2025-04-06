package com.red.yogaback.websocket.service;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import com.red.yogaback.websocket.dto.IceCandidateMessage;

/**
 * WebSocket 연결 상태를 관리하고 ICE 재협상을 트리거하는 서비스입니다.
 */
@Service
public class WebSocketConnectionService {
    // SLF4J 로거 초기화
    private static final Logger logger = LoggerFactory.getLogger(WebSocketConnectionService.class);

    // sessionId -> ConnectionInfo 매핑 (동시성 보장 위해 ConcurrentHashMap 사용)
    private final ConcurrentHashMap<String, ConnectionInfo> activeConnections = new ConcurrentHashMap<>();

    // STOMP 메시지 전송 템플릿
    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketConnectionService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
        // Improvement: 30초마다 체크하던 스케줄러 제거. 필요 시 더 효율적인 감시 메커니즘 도입 고려.
    }

    /**
     * 새로운 연결을 등록합니다.
     *
     * @param sessionId WebSocket 세션 ID
     * @param roomId    방 ID
     * @param userId    사용자 ID
     *
     * Improvement:
     *  - 동시성 등록 시 중복 체크 및 경고 로깅 고려.
     */
    public void registerConnection(String sessionId, String roomId, String userId) {
        activeConnections.put(sessionId, new ConnectionInfo(sessionId, roomId, userId, System.currentTimeMillis()));
        logger.info("Connection registered: sessionId={}, roomId={}, userId={}", sessionId, roomId, userId);
    }

    /**
     * 기존 연결의 활동 시간을 갱신하거나, 없으면 새로 등록합니다.
     *
     * Improvement:
     *  - 활동 시간 갱신만 할지, 아니면 완전 갱신(방/사용자 정보)까지 할지 정책 명확화.
     */
    public void updateConnection(String sessionId, String roomId, String userId) {
        ConnectionInfo existingInfo = activeConnections.get(sessionId);
        if (existingInfo != null) {
            existingInfo.setLastActivityTime(System.currentTimeMillis());
            logger.info("Connection updated: sessionId={}, roomId={}, userId={}", sessionId, roomId, userId);
        } else {
            registerConnection(sessionId, roomId, userId);
        }
    }

    /**
     * 연결을 제거합니다.
     *
     * Improvement:
     *  - 제거 시 리소스 해제(예: 방 인원 감소) 연계 로직 추가 고려.
     */
    public void removeConnection(String sessionId) {
        ConnectionInfo removedInfo = activeConnections.remove(sessionId);
        if (removedInfo != null) {
            logger.info("Connection removed: sessionId={}, roomId={}, userId={}",
                    sessionId, removedInfo.getRoomId(), removedInfo.getUserId());
        }
    }

    /**
     * 특정 세션이 활성 상태인지 확인합니다.
     */
    public boolean isConnectionActive(String sessionId) {
        return activeConnections.containsKey(sessionId);
    }

    /**
     * ICE 후보 정보가 업데이트되면 활동 시간을 갱신합니다.
     *
     * Improvement:
     *  - 실제 후보 내용을 로깅하거나, 후보 검증 로직 추가 가능.
     */
    public void updateIceCandidate(String sessionId, String roomId, String userId) {
        updateConnection(sessionId, roomId, userId);
        logger.info("ICE candidate updated: sessionId={}, roomId={}, userId={}", sessionId, roomId, userId);
    }

    /**
     * ICE 연결 상태 변경을 처리합니다.
     * 예: failed 상태에서 재협상 트리거.
     *
     * Improvement:
     *  - 다양한 상태(state)에 대한 세분화된 처리 로직 추가 고려.
     */
    public void handleIceConnectionStateChange(String sessionId, String state) {
        ConnectionInfo info = activeConnections.get(sessionId);
        if (info != null) {
            if ("failed".equals(state)) {
                // ICE 연결 실패 시 재협상 시도
                triggerIceReconnection(sessionId);
            }
            logger.info("ICE connection state changed for session {}: {}", sessionId, state);
        }
    }

    /**
     * 클라이언트에게 ICE 재협상 요청을 보냅니다.
     *
     * Improvement:
     *  - 재협상 요청 횟수 제한, 백오프 전략 등을 도입해 무한 반복 방지.
     */
    private void triggerIceReconnection(String sessionId) {
        ConnectionInfo info = activeConnections.get(sessionId);
        if (info != null) {
            messagingTemplate.convertAndSendToUser(sessionId, "/queue/ice-restart",
                    new IceCandidateMessage("restart"));
            logger.info("Triggered ICE reconnection for session: {}", sessionId);
        }
    }

    /**
     * 내부 클래스: 연결 정보를 저장합니다.
     */
    private static class ConnectionInfo {
        private final String sessionId;
        private final String roomId;
        private final String userId;
        private long lastActivityTime;

        public ConnectionInfo(String sessionId, String roomId, String userId, long lastActivityTime) {
            this.sessionId = sessionId;
            this.roomId = roomId;
            this.userId = userId;
            this.lastActivityTime = lastActivityTime;
        }

        public String getSessionId() {
            return sessionId;
        }

        public String getRoomId() {
            return roomId;
        }

        public String getUserId() {
            return userId;
        }

        public long getLastActivityTime() {
            return lastActivityTime;
        }

        public void setLastActivityTime(long lastActivityTime) {
            this.lastActivityTime = lastActivityTime;
        }

        /*
         * Improvement:
         *  - toString(), equals(), hashCode() 재정의 시 디버깅 및 Map 키 비교에 유용.
         */
    }
}
