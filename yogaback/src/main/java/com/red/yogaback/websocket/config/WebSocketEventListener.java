package com.red.yogaback.websocket.config;
//클라이언트의 세션 종료(SessionDisconnectEvent)를 감지하여,
//
//해당 세션의 UserSession을 가져와서
//
//방(Room)에서 참가자와 준비 상태를 제거
//
//모든 참가자가 퇴장하면 룸 삭제
//
//다른 사용자에게 퇴장 메시지 전송

import com.red.yogaback.websocket.service.Room;
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
        
        // 기존 로직: 사용자 세션 및 방 처리
        UserSession userSession = userSessionService.getSession(sessionId);
        if (userSession == null) {
            logger.warn("Session not found for sessionId: {}", sessionId);
            return;
        }
        String roomId = userSession.getRoomId();
        String userId = userSession.getUserId();
        Room room = roomService.getRoom(roomId);
        if (room != null) {
            room.removeParticipant(sessionId, userId);
            if (room.getParticipantCount() == 0) {
                roomService.removeRoom(roomId);
                logger.info("Room {} removed as no participants remain", roomId);
            } else {
                messagingTemplate.convertAndSend("/topic/room/" + roomId + "/userLeft",
                        userId + "님이 나갔습니다. 남은 참가자 수: " + room.getParticipantCount());
                logger.info("User {} left room {}", userId, roomId);
            }
        }
        userSessionService.removeSession(sessionId);
    }

    @EventListener
    public void handleWebSocketSubscribeListener(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String destination = headerAccessor.getDestination();
        logger.info("New subscription: {} for session: {}", destination, sessionId);
        
        // 구독 시 연결 정보 업데이트
        UserSession userSession = userSessionService.getSession(sessionId);
        if (userSession != null) {
            connectionService.updateConnection(sessionId, userSession.getRoomId(), userSession.getUserId());
        }
    }

    @EventListener
    public void handleWebSocketUnsubscribeListener(SessionUnsubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        logger.info("Subscription removed for session: {}", sessionId);
    }
}