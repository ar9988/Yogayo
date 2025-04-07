package com.red.yogaback.websocket.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.red.yogaback.service.RoomService;
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

import java.util.HashMap;
import java.util.Map;

@Controller
public class SignalingController {

    private static final Logger logger = LoggerFactory.getLogger(SignalingController.class);

    @Autowired
    private SocketRoomService socketRoomService;

    @Autowired
    private UserSessionService userSessionService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * 클라이언트가 "/app/room/{roomId}"로 메시지를 전송하면 해당 메시지를 받아서,
     * payload를 "/topic/room/{roomId}"로 브로드캐스트하는 메소드입니다.
     * <p>
     * 동작:
     * - @MessageMapping: 클라이언트로부터 수신할 메시지의 경로를 지정합니다.
     * - @DestinationVariable: 경로상의 roomId 값을 변수로 사용합니다.
     * - @Payload: 클라이언트가 전송한 메시지의 내용을 RoomActionMessage 객체로 매핑합니다.
     * - StompHeaderAccessor: 메시지 헤더에서 세션 ID 등 추가 정보를 추출합니다.
     * <p>
     * 개선방향:
     * - 현재 세션 검증 로직은 제거되어 있는데, 필요하다면 사용자 인증이나 권한 확인 로직을 추가할 수 있음.
     * - 메시지 payload에 대한 유효성 검증이나 필터링 처리가 필요할 수 있음.
     * - 로깅 메시지의 상세 수준 및 포맷을 프로젝트 전반에 맞게 일관되게 유지할 수 있음.
     */
    @MessageMapping("/room/{roomId}")
    public void broadcastRoomMessage(@DestinationVariable String roomId,
                                     @Payload RoomActionMessage actionMessage,
                                     StompHeaderAccessor headerAccessor) throws JsonProcessingException {
        String sessionId = headerAccessor.getSessionId();
//        userSessionService.addSession(sessionId,new UserSession(
//                String.valueOf(headerAccessor.getSessionAttributes().get("userId")),
//                roomId,
//                String.valueOf(headerAccessor.getSessionAttributes().get("userNickName")),
//                String.valueOf(headerAccessor.getSessionAttributes().get("userProfile"))));
        logger.debug("Received message for room {} from session {}: {}", roomId, sessionId, actionMessage.getPayload());

        // 개선방향: 세션 검증 로직 제거 상태인데,
        // 필요에 따라 userSessionService를 사용해 세션의 유효성을 검사하는 로직을 추가할 수 있습니다.

        // 메시지 payload를 그대로 해당 room의 토픽으로 브로드캐스트
        messagingTemplate.convertAndSend("/topic/room/" + roomId, actionMessage.getPayload());
        logger.info("Broadcasted message to /topic/room/{} from session {}: {}", roomId, sessionId, actionMessage.getPayload());
//        socketRoomService.addParticipant(roomId);
        logger.info("actionMessage: {}", actionMessage.getPayload().toString());
        ObjectMapper mapper = new ObjectMapper();
        String type = parsingActionMessage(actionMessage.getPayload().toString());
        logger.info("type: {}", type);
        if (type.equals("user_joined")){
            socketRoomService.addParticipant(roomId);
        }

    }

    public String parsingActionMessage(String raw) {
        raw = raw.replaceAll("[{} ]", "");
        String[] entries = raw.split(",");

        Map<String, String> map = new HashMap<>();
        for (String entry : entries) {
            String[] keyValue = entry.split("=");
            if (keyValue.length == 2) {
                map.put(keyValue[0], keyValue[1]);
            }
        }

        return map.get("type");
    }


}
