package com.red.yogaback.websocket.config;

import com.red.yogaback.websocket.service.Room;
import com.red.yogaback.websocket.service.RoomService;
import com.red.yogaback.websocket.service.UserSession;
import com.red.yogaback.websocket.service.UserSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@Component
public class WebSocketEventListener implements ApplicationListener<SessionDisconnectEvent> {

    @Autowired
    private UserSessionService userSessionService;

    @Autowired
    private RoomService roomService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // 재연결 유예 기간 (여기서는 단순 구현을 위해 바로 제거)
    private final long RECONNECT_GRACE_PERIOD = 10000;

    @Override
    public void onApplicationEvent(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        UserSession userSession = userSessionService.getSession(sessionId);
        if (userSession == null) return;
        String roomId = userSession.getRoomId();
        String userId = userSession.getUserId();

        Room room = roomService.getRoom(roomId);
        if (room != null) {
            room.getParticipants().remove(sessionId);
            room.getReadyUsers().remove(userId);
            if (room.getParticipants().isEmpty()) {
                roomService.removeRoom(roomId);
            } else {
                messagingTemplate.convertAndSend("/topic/room/" + roomId + "/userLeft",
                        userId + "님이 나갔습니다. 남은 참가자 수: " + room.getParticipants().size());
            }
        }
        userSessionService.removeSession(sessionId);
    }
}