package com.red.yogaback.websocket.controller;

import com.red.yogaback.websocket.dto.RoomActionMessage;
import com.red.yogaback.websocket.service.UserSession;
import com.red.yogaback.websocket.service.UserSessionService;
import com.red.yogaback.websocket.service.SocketRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class SignalingController {

    private static final Logger logger = LoggerFactory.getLogger(SignalingController.class);

    @Autowired
    private SocketRoomService socketRoomService;

    @Autowired
    private UserSessionService userSessionService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // 세션 검증 메서드 (필요 시 유지)
    private UserSession getValidatedUserSession(StompHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        UserSession userSession = userSessionService.getSession(sessionId);
        if (userSession == null || userSession.getRoomId() == null) {
            logger.warn("No room information found for session: {}", sessionId);
            messagingTemplate.convertAndSendToUser(sessionId, "/queue/errors", "방 정보가 없습니다.");
            return null;
        }
        return userSession;
    }

    // 새로운 엔드포인트: "/app/room/{roomId}"
    // 받은 메시지를 그대로 "/topic/room/{roomId}"로 브로드캐스트합니다.
    @MessageMapping("/room/{roomId}")
    public void broadcastRoomMessage(@DestinationVariable String roomId,
                                     @Payload RoomActionMessage actionMessage,
                                     StompHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        logger.debug("Received message for room {} from session {}: {}", roomId, sessionId, actionMessage.getPayload());

        // 세션 검증 로직 제거

        // 메시지를 그대로 브로드캐스트합니다.
        messagingTemplate.convertAndSend("/topic/room/" + roomId, actionMessage.getPayload());
        logger.info("Broadcasted message to /topic/room/{} from session {}: {}", roomId, sessionId, actionMessage.getPayload());
    }

}
