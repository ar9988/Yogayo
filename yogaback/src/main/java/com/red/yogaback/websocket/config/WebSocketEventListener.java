package com.red.yogaback.websocket.config;

import com.red.yogaback.websocket.service.SocketRoomService;
import com.red.yogaback.websocket.service.UserSession;
import com.red.yogaback.websocket.service.UserSessionService;
import com.red.yogaback.websocket.service.WebSocketConnectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class WebSocketEventListener {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);

    @Autowired
    private UserSessionService userSessionService;

    @Autowired
    private SocketRoomService roomService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private WebSocketConnectionService connectionService;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        logger.info("New WebSocket connection established: {}", sessionId);
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        logger.info("WebSocket connection closed: {}", sessionId);

        // 연결 종료 처리
        connectionService.removeConnection(sessionId);

        // 사용자 세션 조회 후 DB 업데이트: 방에서 퇴장 처리
        UserSession userSession = userSessionService.getSession(sessionId);
        if (userSession == null) {
            logger.warn("Session not found for sessionId: {}", sessionId);
            return;
        }
        String roomId = userSession.getRoomId(); // 문자열 형태라고 가정
        String userId = userSession.getUserId();

        // DB 기반 방에서 해당 사용자의 퇴장을 반영
        roomService.removeParticipant(roomId);
        // 선택적으로 퇴장 알림을 발송
        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/userLeft",
                userId + "님이 나갔습니다.");
        logger.info("User {} left room {}", userId, roomId);

        userSessionService.removeSession(sessionId);
    }

    @EventListener
    public void handleWebSocketSubscribeListener(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String destination = headerAccessor.getDestination();
        logger.info("New subscription: {} for session: {}", destination, sessionId);

        // 구독 시 연결 정보 업데이트 (UserSession이 이미 등록되어 있다고 가정)
        UserSession userSession = userSessionService.getSession(sessionId);
        if (userSession != null) {
            connectionService.updateConnection(sessionId, userSession.getRoomId(), userSession.getUserId());
            // DB 기반 방에 참가자 추가
            roomService.addParticipant(userSession.getRoomId());
        }
    }

    @EventListener
    public void handleWebSocketUnsubscribeListener(SessionUnsubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        logger.info("Subscription removed for session: {}", sessionId);
    }
}
