package com.red.yogaback.websocket.service;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import com.red.yogaback.websocket.dto.IceCandidateMessage;


@Service
public class WebSocketConnectionService {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketConnectionService.class);
    private final ConcurrentHashMap<String, ConnectionInfo> activeConnections = new ConcurrentHashMap<>();
    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketConnectionService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
        // 기존 30초마다 연결 상태 체크하는 스케줄러 제거
    }

    public void registerConnection(String sessionId, String roomId, String userId) {
        activeConnections.put(sessionId, new ConnectionInfo(sessionId, roomId, userId, System.currentTimeMillis()));
        logger.info("Connection registered: sessionId={}, roomId={}, userId={}", sessionId, roomId, userId);
    }

    public void updateConnection(String sessionId, String roomId, String userId) {
        ConnectionInfo existingInfo = activeConnections.get(sessionId);
        if (existingInfo != null) {
            existingInfo.setLastActivityTime(System.currentTimeMillis());
            logger.info("Connection updated: sessionId={}, roomId={}, userId={}", sessionId, roomId, userId);
        } else {
            registerConnection(sessionId, roomId, userId);
        }
    }

    public void removeConnection(String sessionId) {
        ConnectionInfo removedInfo = activeConnections.remove(sessionId);
        if (removedInfo != null) {
            logger.info("Connection removed: sessionId={}, roomId={}, userId={}",
                    sessionId, removedInfo.getRoomId(), removedInfo.getUserId());
        }
    }

    public boolean isConnectionActive(String sessionId) {
        return activeConnections.containsKey(sessionId);
    }

    public void updateIceCandidate(String sessionId, String roomId, String userId) {
        // ICE 후보 변경 시 연결 정보 업데이트
        updateConnection(sessionId, roomId, userId);
        logger.info("ICE candidate updated: sessionId={}, roomId={}, userId={}", sessionId, roomId, userId);
    }

    public void handleIceConnectionStateChange(String sessionId, String state) {
        ConnectionInfo info = activeConnections.get(sessionId);
        if (info != null) {
            if ("failed".equals(state)) {
                // ICE 연결 실패 시 재연결 시도
                triggerIceReconnection(sessionId);
            }
            logger.info("ICE connection state changed for session {}: {}", sessionId, state);
        }
    }

    private void triggerIceReconnection(String sessionId) {
        ConnectionInfo info = activeConnections.get(sessionId);
        if (info != null) {
            // 클라이언트에게 ICE 재협상 요청 메시지 전송
            messagingTemplate.convertAndSendToUser(sessionId, "/queue/ice-restart",
                    new IceCandidateMessage("restart"));
            logger.info("Triggered ICE reconnection for session: {}", sessionId);
        }
    }

    // 연결 정보를 저장하는 내부 클래스
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
    }
}
