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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class WebSocketEventListener implements ApplicationListener<SessionDisconnectEvent> {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);

    @Autowired
    private UserSessionService userSessionService;

    @Autowired
    private SocketRoomService roomService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Override
    public void onApplicationEvent(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
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
}