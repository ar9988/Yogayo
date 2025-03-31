package com.red.yogaback.websocket.controller;

import com.red.yogaback.websocket.model.JoinRoomMessage;
import com.red.yogaback.websocket.model.ReadyMessage;
import com.red.yogaback.websocket.model.SignalMessage;
import com.red.yogaback.websocket.model.IceCandidateMessage;
import com.red.yogaback.websocket.service.Room;
import com.red.yogaback.websocket.service.RoomService;
import com.red.yogaback.websocket.service.UserSession;
import com.red.yogaback.websocket.service.UserSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;

@Controller
public class SignalingController {

    @Autowired
    private RoomService roomService;

    @Autowired
    private UserSessionService userSessionService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // 방 참가 이벤트 처리 (/app/joinRoom)
    @MessageMapping("/joinRoom")
    public void joinRoom(@Payload JoinRoomMessage joinRoomMessage, StompHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        // 세션 속성에서 인증된 사용자 정보 추출
        String userId = (String) headerAccessor.getSessionAttributes().get("userId");
        String userNickName = (String) headerAccessor.getSessionAttributes().get("userNickName");
        String userProfile = (String) headerAccessor.getSessionAttributes().get("userProfile");
        String roomId = joinRoomMessage.getRoomId();

        Room room = roomService.getRoom(roomId);
        if (room == null) {
            messagingTemplate.convertAndSendToUser(sessionId, "/queue/errors", "존재하지 않는 방입니다.");
            return;
        }

        if (room.getParticipants().containsKey(sessionId)) {
            messagingTemplate.convertAndSendToUser(sessionId, "/queue/errors", "이미 방에 참가하였습니다.");
            return;
        }

        room.getParticipants().put(sessionId, userId);
        userSessionService.addSession(sessionId, new UserSession(userId, roomId, userNickName, userProfile));

        // 방에 참가한 사용자 정보를 해당 방의 토픽으로 브로드캐스트
        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/userJoined",
                new UserSession(userId, roomId, userNickName, userProfile));
    }

    // 준비 상태 이벤트 (/app/ready)
    @MessageMapping("/ready")
    public void ready(StompHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        UserSession userSession = userSessionService.getSession(sessionId);
        if (userSession == null || userSession.getRoomId() == null) {
            messagingTemplate.convertAndSendToUser(sessionId, "/queue/errors", "방 정보가 없습니다.");
            return;
        }
        String roomId = userSession.getRoomId();
        Room room = roomService.getRoom(roomId);
        if (room == null) {
            messagingTemplate.convertAndSendToUser(sessionId, "/queue/errors", "존재하지 않는 방입니다.");
            return;
        }
        room.getReadyUsers().add(userSession.getUserId());

        if (room.getReadyUsers().size() == room.getParticipants().size()) {
            room.setCourseStarted(true);
            messagingTemplate.convertAndSend("/topic/room/" + roomId + "/allReady", "모든 참가자가 준비 완료되었습니다.");
            messagingTemplate.convertAndSend("/topic/room/" + roomId + "/courseStarted", "코스가 시작되었습니다.");
        } else {
            messagingTemplate.convertAndSend("/topic/room/" + roomId + "/userReady",
                    userSession.getUserId() + " 준비 완료. 현재 준비 인원: " + room.getReadyUsers().size());
        }
    }

    // 시그널링 메시지 이벤트 (/app/signal)
    @MessageMapping("/signal")
    public void signal(@Payload SignalMessage signalMessage, StompHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        UserSession userSession = userSessionService.getSession(sessionId);
        if (userSession == null || userSession.getRoomId() == null) {
            messagingTemplate.convertAndSendToUser(sessionId, "/queue/errors", "방 정보가 없습니다.");
            return;
        }
        String roomId = userSession.getRoomId();
        // 시그널 메시지를 해당 방의 구독자들에게 전달 (발신자 정보 포함)
        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/signal",
                new Object() {
                    public final String userId = userSession.getUserId();
                    public final String userNickName = userSession.getUserNickName();
                    public final String userProfile = userSession.getUserProfile();
                    public final String signal = signalMessage.getSignal();
                });
    }

    // ICE 후보 교환 이벤트 (/app/iceCandidate)
    @MessageMapping("/iceCandidate")
    public void iceCandidate(@Payload IceCandidateMessage candidateMessage, StompHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        UserSession userSession = userSessionService.getSession(sessionId);
        if (userSession == null || userSession.getRoomId() == null) {
            messagingTemplate.convertAndSendToUser(sessionId, "/queue/errors", "방 정보가 없습니다.");
            return;
        }
        String roomId = userSession.getRoomId();
        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/iceCandidate",
                new Object() {
                    public final String userId = userSession.getUserId();
                    public final String userNickName = userSession.getUserNickName();
                    public final String userProfile = userSession.getUserProfile();
                    public final String candidate = candidateMessage.getCandidate();
                });
    }

    // Heartbeat 이벤트 (/app/heartbeat)
    @MessageMapping("/heartbeat")
    public void heartbeat(StompHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        UserSession userSession = userSessionService.getSession(sessionId);
        if (userSession == null || userSession.getRoomId() == null) {
            return;
        }
        String roomId = userSession.getRoomId();
        // 다른 참가자에게 heartbeat 신호 전달
        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/heartbeat",
                userSession.getUserId());
    }
}