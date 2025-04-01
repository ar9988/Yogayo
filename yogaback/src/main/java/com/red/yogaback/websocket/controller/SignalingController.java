package com.red.yogaback.websocket.controller;
//WebSocket 메시지 핸들러로 방 참가, 준비, 시그널 전달, ICE 후보 전달, heartbeat 이벤트 등을 처리합니다.
//
//각 메서드 내에서 사용자 세션과 방 존재 여부를 확인한 후, 해당 메시지를 처리합니다.
import com.red.yogaback.websocket.dto.IceCandidateDTO;
import com.red.yogaback.websocket.dto.UserSignalDTO;
import com.red.yogaback.websocket.dto.JoinRoomMessage;
import com.red.yogaback.websocket.dto.SignalMessage;
import com.red.yogaback.websocket.dto.IceCandidateMessage;
import com.red.yogaback.websocket.service.Room;
import com.red.yogaback.websocket.service.SocketRoomService;
import com.red.yogaback.websocket.service.UserSession;
import com.red.yogaback.websocket.service.UserSessionService;
import org.springframework.beans.factory.annotation.Autowired;
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
    private SocketRoomService roomService;

    @Autowired
    private UserSessionService userSessionService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // 공통 유효성 검사 헬퍼 메서드
    private UserSession getValidatedUserSession(StompHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        UserSession userSession = userSessionService.getSession(sessionId);
        if (userSession == null || userSession.getRoomId() == null) {
            messagingTemplate.convertAndSendToUser(sessionId, "/queue/errors", "방 정보가 없습니다.");
            return null;
        }
        return userSession;
    }

    @MessageMapping("/joinRoom")
    public void joinRoom(@Payload JoinRoomMessage joinRoomMessage, StompHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        String roomId = joinRoomMessage.getRoomId();
        Room room = roomService.getRoom(roomId);
        if (room == null) {
            messagingTemplate.convertAndSendToUser(sessionId, "/queue/errors", "존재하지 않는 방입니다.");
            return;
        }

        if (room.hasParticipant(sessionId)) {
            messagingTemplate.convertAndSendToUser(sessionId, "/queue/errors", "이미 방에 참가하였습니다.");
            return;
        }

        // 세션 속성에서 사용자 정보 추출
        String userId = (String) headerAccessor.getSessionAttributes().get("userId");
        String userNickName = (String) headerAccessor.getSessionAttributes().get("userNickName");
        String userProfile = (String) headerAccessor.getSessionAttributes().get("userProfile");

        room.addParticipant(sessionId, userId);
        userSessionService.addSession(sessionId, new UserSession(userId, roomId, userNickName, userProfile));

        // 참가한 사용자 정보를 브로드캐스트
        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/userJoined",
                new UserSession(userId, roomId, userNickName, userProfile));
        logger.info("User {} joined room {}", userId, roomId);
    }

    @MessageMapping("/ready")
    public void ready(StompHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        UserSession userSession = getValidatedUserSession(headerAccessor);
        if (userSession == null) return;

        String roomId = userSession.getRoomId();
        Room room = roomService.getRoom(roomId);
        if (room == null) {
            messagingTemplate.convertAndSendToUser(sessionId, "/queue/errors", "존재하지 않는 방입니다.");
            return;
        }
        room.addReadyUser(userSession.getUserId());

        if (room.allUsersReady()) {
            room.setCourseStarted(true);
            messagingTemplate.convertAndSend("/topic/room/" + roomId + "/allReady", "모든 참가자가 준비 완료되었습니다.");
            messagingTemplate.convertAndSend("/topic/room/" + roomId + "/courseStarted", "코스가 시작되었습니다.");
            logger.info("All users in room {} are ready. Course started.", roomId);
        } else {
            messagingTemplate.convertAndSend("/topic/room/" + roomId + "/userReady",
                    userSession.getUserId() + " 준비 완료. 현재 준비 인원: " + room.getReadyUserCount());
            logger.info("User {} is ready in room {}", userSession.getUserId(), roomId);
        }
    }

    @MessageMapping("/signal")
    public void signal(@Payload SignalMessage signalMessage, StompHeaderAccessor headerAccessor) {
        UserSession userSession = getValidatedUserSession(headerAccessor);
        if (userSession == null) return;
        String roomId = userSession.getRoomId();

        UserSignalDTO dto = new UserSignalDTO(
                userSession.getUserId(),
                userSession.getUserNickName(),
                userSession.getUserProfile(),
                signalMessage.getSignal()
        );
        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/signal", dto);
        logger.info("Signal message from user {} in room {}", userSession.getUserId(), roomId);
    }

    @MessageMapping("/iceCandidate")
    public void iceCandidate(@Payload IceCandidateMessage candidateMessage, StompHeaderAccessor headerAccessor) {
        UserSession userSession = getValidatedUserSession(headerAccessor);
        if (userSession == null) return;
        String roomId = userSession.getRoomId();

        IceCandidateDTO dto = new IceCandidateDTO(
                userSession.getUserId(),
                userSession.getUserNickName(),
                userSession.getUserProfile(),
                candidateMessage.getCandidate()
        );
        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/iceCandidate", dto);
        logger.info("ICE candidate message from user {} in room {}", userSession.getUserId(), roomId);
    }

    @MessageMapping("/heartbeat")
    public void heartbeat(StompHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        UserSession userSession = userSessionService.getSession(sessionId);
        if (userSession == null || userSession.getRoomId() == null) return;
        String roomId = userSession.getRoomId();
        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/heartbeat", userSession.getUserId());
        logger.debug("Heartbeat received from user {} in room {}", userSession.getUserId(), roomId);
    }
}
