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
    public void handleWebSocketSubscribeListener(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String destination = headerAccessor.getDestination();
        logger.info("New subscription: {} for session: {}", destination, sessionId);

        // destination 이 "/topic/room/{roomId}" 패턴이면 처리
        if (destination != null && destination.matches("/topic/room/\\d+")) {
            String roomId = destination.substring(destination.lastIndexOf('/') + 1);
            UserSession userSession = userSessionService.getSession(sessionId);
            if (userSession != null) {
                // 기존 세션은 불변이므로 roomId가 추가된 새 객체로 재등록
                UserSession updatedSession = new UserSession(
                        userSession.getUserId(),
                        roomId,
                        userSession.getUserNickName(),
                        userSession.getUserProfile()
                );
                userSessionService.addSession(sessionId, updatedSession);
                // 연결 등록
                connectionService.registerConnection(sessionId, roomId, userSession.getUserId());
                // 방 참가자 수 증가 및 입장 메시지 전송
                roomService.addParticipant(roomId);
                messagingTemplate.convertAndSend("/topic/room/" + roomId,
                        userSession.getUserNickName() + "님이 들어왔습니다.");
                logger.info("Processed subscription for room: {} and session: {}", roomId, sessionId);
            } else {
                logger.warn("No user session found for session: {}", sessionId);
            }
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {

        try {
            StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
            String sessionId = headerAccessor.getSessionId();
            logger.info(">> Disconnect listener entered for session: {}", sessionId);
            logger.debug("Disconnect event received for session: {}", sessionId);

            // 연결 정보 제거

            UserSession userSession = userSessionService.getSession(sessionId);

            if (userSession == null) {
                logger.warn("Session not found for sessionId: {}", sessionId);
                return;
            }
            String roomId = userSession.getRoomId();
            String userNickName = userSession.getUserNickName();
            logger.debug("Found user session: {}", userSession);

            // 방 참가자 수 감소 및 퇴장 메시지 전송
            roomService.removeParticipant(roomId);
            messagingTemplate.convertAndSend("/topic/room/" + roomId,
                    userNickName + "님이 나갔습니다.");
            // 세션 정보 제거
            userSessionService.removeSession(sessionId);
            connectionService.removeConnection(sessionId);
            logger.debug("Processed disconnect for session: {}", sessionId);
        } catch (Exception e) {
            logger.error("Error handling WebSocket disconnect: {}", e.getMessage(), e);
        }
    }

    @EventListener
    public void handleWebSocketUnsubscribeListener(SessionUnsubscribeEvent event) {
        // Unsubscribe 이벤트는 별도 처리 없이 로그만 남기거나,
        // Disconnect 이벤트에서 처리하도록 할 수 있습니다.
        String sessionId = StompHeaderAccessor.wrap(event.getMessage()).getSessionId();
        logger.info("Unsubscribe event received for session: {}", sessionId);
    }
}
