package com.red.yogaback.websocket.controller;

import com.red.yogaback.websocket.dto.IceCandidateDTO;
import com.red.yogaback.websocket.dto.RoomActionMessage;
import com.red.yogaback.websocket.dto.UserSignalDTO;
import com.red.yogaback.websocket.dto.JoinRoomMessage;
import com.red.yogaback.websocket.dto.SignalMessage;
import com.red.yogaback.websocket.dto.IceCandidateMessage;
import com.red.yogaback.websocket.dto.ReadyMessage;// 필요하다면 준비용 DTO 추가
import com.red.yogaback.websocket.service.Room;
import com.red.yogaback.websocket.service.SocketRoomService;
import com.red.yogaback.websocket.service.UserSession;
import com.red.yogaback.websocket.service.UserSessionService;
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

    // 통합 엔드포인트: 클라이언트는 "/app/action/{roomId}"로 메시지를 보냅니다.
    @MessageMapping("/action/{roomId}")
    public void handleRoomAction(@DestinationVariable String roomId,
                                 @Payload RoomActionMessage actionMessage,
                                 StompHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        // 유효한 사용자 세션 확인
        UserSession userSession = getValidatedUserSession(headerAccessor);
        if (userSession == null) return;
        // 경로 변수의 roomId와 세션에 저장된 roomId가 일치하는지 확인
        if (!roomId.equals(userSession.getRoomId())) {
            messagingTemplate.convertAndSendToUser(sessionId, "/queue/errors", "요청한 방 정보와 세션의 방 정보가 일치하지 않습니다.");
            return;
        }

        String action = actionMessage.getAction();
        switch (action) {
            case "enter":
                // "enter" 액션: JoinRoomMessage DTO를 사용
                // (경로 변수 roomId를 사용하므로, payload 내 roomId는 무시할 수 있음)
                JoinRoomMessage joinMsg = (JoinRoomMessage) actionMessage.getMessage();
                Room room = socketRoomService.getRoom(roomId);
                if (room == null) {
                    messagingTemplate.convertAndSendToUser(sessionId, "/queue/errors", "존재하지 않는 방입니다.");
                    return;
                }
                if (room.hasParticipant(sessionId)) {
                    messagingTemplate.convertAndSendToUser(sessionId, "/queue/errors", "이미 방에 참가하였습니다.");
                    return;
                }
                // WebSocketAuthChannelInterceptor에서 이미 세션 속성에 저장된 값 사용
                String userId = (String) headerAccessor.getSessionAttributes().get("userId");
                String userNickName = (String) headerAccessor.getSessionAttributes().get("userNickName");
                String userProfile = (String) headerAccessor.getSessionAttributes().get("userProfile");
                room.addParticipant(sessionId, userId);
                userSessionService.addSession(sessionId, new UserSession(userId, roomId, userNickName, userProfile));
                // 방 참가 정보를 브로드캐스트 (구독 경로: /topic/room/{roomId})
                messagingTemplate.convertAndSend("/topic/room/" + roomId, new UserSession(userId, roomId, userNickName, userProfile));
                logger.info("User {} joined room {}", userId, roomId);
                break;

            case "ready":
                room = socketRoomService.getRoom(roomId);
                if (room == null) {
                    messagingTemplate.convertAndSendToUser(sessionId, "/queue/errors", "존재하지 않는 방입니다.");
                    return;
                }
                // ReadyMessage DTO로 캐스팅
                ReadyMessage readyMsg = (ReadyMessage) actionMessage.getMessage();
                // isReady 값에 따라 준비 상태 추가/제거 처리
                if (readyMsg.isReady()) {
                    room.addReadyUser(userSession.getUserId());
                } else {
                    room.removeReadyUser(userSession.getUserId());
                }
                // 모든 사용자가 준비되었는지 확인
                if (room.allUsersReady()) {
                    room.setCourseStarted(true);
                    messagingTemplate.convertAndSend("/topic/room/" + roomId + "/allReady", "모든 참가자가 준비 완료되었습니다.");
                    messagingTemplate.convertAndSend("/topic/room/" + roomId + "/courseStarted", "코스가 시작되었습니다.");
                    logger.info("All users in room {} are ready. Course started.", roomId);
                } else {
                    messagingTemplate.convertAndSend("/topic/room/" + roomId + "/userReady",
                            userSession.getUserId() + " 준비 완료. 현재 준비 인원: " + room.getReadyUserCount());
                    logger.info("User {} readiness changed in room {}", userSession.getUserId(), roomId);
                }
                break;

            case "signal":
                // "signal" 액션: SignalMessage DTO를 사용하여 시그널 처리
                SignalMessage signalMsg = (SignalMessage) actionMessage.getMessage();
                UserSignalDTO signalDto = new UserSignalDTO(
                        userSession.getUserId(),
                        userSession.getUserNickName(),
                        userSession.getUserProfile(),
                        signalMsg.getSignal()
                );
                messagingTemplate.convertAndSend("/topic/room/" + roomId + "/signal", signalDto);
                logger.info("Signal message from user {} in room {}", userSession.getUserId(), roomId);
                break;

            case "iceCandidate":
                // "iceCandidate" 액션: IceCandidateMessage DTO를 사용하여 ICE 후보 처리
                IceCandidateMessage candidateMsg = (IceCandidateMessage) actionMessage.getMessage();
                IceCandidateDTO candidateDto = new IceCandidateDTO(
                        userSession.getUserId(),
                        userSession.getUserNickName(),
                        userSession.getUserProfile(),
                        candidateMsg.getCandidate()
                );
                messagingTemplate.convertAndSend("/topic/room/" + roomId + "/iceCandidate", candidateDto);
                logger.info("ICE candidate message from user {} in room {}", userSession.getUserId(), roomId);
                break;

            case "heartbeat":
                // "heartbeat" 액션: 추가 payload 없이 단순히 heartbeat 처리
                messagingTemplate.convertAndSend("/topic/room/" + roomId + "/heartbeat", userSession.getUserId());
                logger.debug("Heartbeat received from user {} in room {}", userSession.getUserId(), roomId);
                break;

            default:
                // 알 수 없는 액션 처리
                messagingTemplate.convertAndSendToUser(sessionId, "/queue/errors", "알 수 없는 action입니다.");
                logger.warn("Unknown action '{}' from session {}", action, sessionId);
                break;
        }
    }
}
