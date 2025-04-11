package com.red.yogaback.websocket.service;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import com.red.yogaback.websocket.dto.IceCandidateMessage;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Map;

// 추가: DISCONNECT 이벤트 트리거를 위한 클래스들
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;

@Service
public class WebSocketConnectionService {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketConnectionService.class);

    // sessionId -> ConnectionInfo 매핑 (동시성 보장을 위해 ConcurrentHashMap 사용)
    private final ConcurrentHashMap<String, ConnectionInfo> activeConnections = new ConcurrentHashMap<>();
    private final SimpMessagingTemplate messagingTemplate;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    private UserSessionService userSessionService;

    public WebSocketConnectionService(SimpMessagingTemplate messagingTemplate,
                                      ApplicationEventPublisher eventPublisher) {
        this.messagingTemplate = messagingTemplate;
        this.eventPublisher = eventPublisher;
    }

    // 기존 메서드들 (registerConnection, updateConnection, removeConnection 등)

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

    public void updateIceCandidate(String sessionId, String roomId, String userId) {
        updateConnection(sessionId, roomId, userId);
        logger.info("ICE candidate updated: sessionId={}, roomId={}, userId={}", sessionId, roomId, userId);
    }

    public void handleIceConnectionStateChange(String sessionId, String state) {
        ConnectionInfo info = activeConnections.get(sessionId);
        if (info != null) {
            if ("failed".equals(state)) {
                triggerIceReconnection(sessionId);
            }
            logger.info("ICE connection state changed for session {}: {}", sessionId, state);
        }
    }

    private void triggerIceReconnection(String sessionId) {
        ConnectionInfo info = activeConnections.get(sessionId);
        if (info != null) {
            messagingTemplate.convertAndSendToUser(sessionId, "/queue/ice-restart",
                    new IceCandidateMessage("restart"));
            logger.info("Triggered ICE reconnection for session: {}", sessionId);
        }
    }

    /**
     * 10초마다 활성 연결들 중 1분 이상 활동(heartbeat 포함)이 없으면
     * 해당 세션에 대해 DISCONNECT 이벤트를 발생시킵니다.
     */
    @Scheduled(fixedDelay = 10000)
    public void checkInactiveConnections() {
        long now = System.currentTimeMillis();
        for (Map.Entry<String, ConnectionInfo> entry : activeConnections.entrySet()) {
            String sessionId = entry.getKey();
            ConnectionInfo info = entry.getValue();
            if (now - info.getLastActivityTime() > 60000) {
                logger.warn("No heartbeat for session {} over 1min → triggering DISCONNECT", sessionId);

                // 1) StompHeaderAccessor 생성
                StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.DISCONNECT);
                accessor.setSessionId(sessionId);

                // 2) 메시지 빌드 (방법 A: setHeaders(accessor))
                Message<byte[]> message = MessageBuilder
                        .withPayload(new byte[0])
                        .setHeaders(accessor)
                        .build();

                // (방법 B: createMessage 사용 시)
                // Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.toMessageHeaders());

                // 3) 이벤트 발행 → WebSocketEventListener.handleWebSocketDisconnectListener() 호출됨
                SessionDisconnectEvent event = new SessionDisconnectEvent(
                        this,
                        message,
                        sessionId,
                        CloseStatus.NORMAL
                );
                eventPublisher.publishEvent(event);

                // 4) 내부 맵에서 제거
                activeConnections.remove(sessionId);
            }
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
    }
}
