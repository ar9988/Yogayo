//WebSocket 관련 이벤트(연결, 연결 종료, 구독, 구독 취소)를 처리하여 사용자 세션 관리,
// 방 참여자 수 업데이트, 알림 메시지 발송 등의 작업을 수행합니다.
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
import org.springframework.web.socket.CloseStatus;

@Component
public class WebSocketEventListener {

    // SLF4J 로거 초기화
    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);

    @Autowired
    private UserSessionService userSessionService;

    @Autowired
    private SocketRoomService roomService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private WebSocketConnectionService connectionService;

    /**
     * 클라이언트가 WebSocket 연결을 성공했을 때 호출되는 이벤트 리스너.
     * 개선방향:
     *  - 연결 후 추가적인 초기화 작업(예: 세션 정보 저장)이 필요할 경우 로직 추가 고려.
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        // STOMP 헤더 접근자를 사용하여 세션 ID 추출
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        logger.info("New WebSocket connection established: {}", sessionId);
    }

    /**
     * 클라이언트가 WebSocket 연결을 종료했을 때 호출되는 이벤트 리스너.
     * 개선방향:
     *  - 연결 종료 시 예외 상황(예: DB 업데이트 실패)에 대한 추가 예외 처리를 고려.
     *  - 로그 메시지에 민감한 정보가 포함되지 않도록 주의.
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        try {
            StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
            String sessionId = headerAccessor.getSessionId();
            logger.debug("Disconnect event received for session: {}", sessionId);
            
            // 1. 연결 정보 제거
            connectionService.removeConnection(sessionId);
            logger.debug("Connection removed for session: {}", sessionId);
            
            // 2. 사용자 세션 정보 조회
            UserSession userSession = userSessionService.getSession(sessionId);
            if (userSession == null) {
                logger.warn("Session not found for sessionId: {}", sessionId);
                return;
            }
            logger.debug("Found user session: {}", userSession);
            
            String roomId = userSession.getRoomId();
            String userId = userSession.getUserId();

            // 3. 방 참가자 수 감소 전 로깅
            logger.debug("Attempting to remove participant from room: {}", roomId);
            roomService.removeParticipant(roomId);
            logger.debug("Successfully removed participant from room: {}", roomId);
            
            // 4. 퇴장 메시지 전송
            messagingTemplate.convertAndSend("/topic/room/" + roomId + "/userLeft",
                    userId + "님이 나갔습니다.");
            logger.debug("Sent leave message for user: {} in room: {}", userId, roomId);
            
            // 5. 사용자 세션 정보 제거
            userSessionService.removeSession(sessionId);
            logger.debug("Removed user session for: {}", sessionId);
            
        } catch (Exception e) {
            logger.error("Error handling WebSocket disconnect: {}", e.getMessage(), e);
            // 스택 트레이스도 출력
            logger.error("Full stack trace:", e);
        }
    }

    /**
     * 클라이언트가 특정 채널을 구독할 때 호출되는 이벤트 리스너.
     * 개선방향:
     *  - 구독한 대상(destination)에 대한 유효성 검증 추가 고려.
     *  - 사용자 세션이 등록되어 있지 않은 경우에 대한 대처 로직 확장 가능.
     */
    @EventListener
    public void handleWebSocketSubscribeListener(SessionSubscribeEvent event) {
        // STOMP 헤더 접근자를 사용하여 세션 ID와 구독 대상(destination) 추출
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String destination = headerAccessor.getDestination();
        logger.info("New subscription: {} for session: {}", destination, sessionId);

        // 구독 시 연결 정보 업데이트 (UserSession이 이미 등록되어 있다고 가정)
        UserSession userSession = userSessionService.getSession(sessionId);
        logger.info("userSession : {}",userSession);
        if (userSession != null) {
            logger.info("userSession : {}",userSession);
            connectionService.updateConnection(sessionId, userSession.getRoomId(), userSession.getUserId());
            // DB 기반 방에 참가자 추가
            roomService.addParticipant(userSession.getRoomId());
        }
    }

    /**
     * 클라이언트가 채널 구독을 취소할 때 호출되는 이벤트 리스너.
     * 개선방향:
     *  - 구독 취소 후 추가적인 정리 작업(예: 구독 관련 캐시 삭제) 고려.
     */
    @EventListener
    public void handleWebSocketUnsubscribeListener(SessionUnsubscribeEvent event) {
        try {
            StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
            String sessionId = headerAccessor.getSessionId();
            logger.info("Subscription removed for session: {}", sessionId);

            // 1. 사용자 세션 정보 조회
            UserSession userSession = userSessionService.getSession(sessionId);
            if (userSession != null) {
                String roomId = userSession.getRoomId();
                String userId = userSession.getUserId();

                // 2. 방 참가자 수 감소
                roomService.removeParticipant(roomId);

                // 3. 방에 있는 다른 사용자들에게 퇴장 메시지 전송
                messagingTemplate.convertAndSend("/topic/room/" + roomId + "/userLeft",
                        userId + "님이 나갔습니다.");

                // 4. 연결 정보 제거
                connectionService.removeConnection(sessionId);
                
                // 5. 사용자 세션 정보 제거
                userSessionService.removeSession(sessionId);

                // 6. 클라이언트에게 연결 종료 요청 전송
                messagingTemplate.convertAndSendToUser(sessionId, "/queue/disconnect", 
                    "Connection closed due to unsubscribe");
            }
        } catch (Exception e) {
            logger.error("Error handling WebSocket unsubscribe: {}", e.getMessage(), e);
        }
    }
}
