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

@Controller
public class SignalingController {

    private static final Logger logger = LoggerFactory.getLogger(SignalingController.class);

    @Autowired
    private SocketRoomService socketRoomService;

    @Autowired
    private UserSessionService userSessionService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // 새로운 엔드포인트: "/app/room/{roomId}"
    // 받은 메시지를 그대로 "/topic/room/{roomId}"로 브로드캐스트합니다.
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

        // 세션 검증 로직 제거

        // 메시지를 그대로 브로드캐스트합니다.
        messagingTemplate.convertAndSend("/topic/room/" + roomId, actionMessage.getPayload());
        logger.info("Broadcasted message to /topic/room/{} from session {}: {}", roomId, sessionId, actionMessage.getPayload());
//        socketRoomService.addParticipant(roomId);
        logger.info("actionMessage: {}", actionMessage.getPayload().toString() );
        ObjectMapper mapper = new ObjectMapper();
        ActionPayload payloadObj = mapper.readValue(actionMessage.getPayload().toString(), ActionPayload.class);
        String type = payloadObj.getType();
        logger.info("type: {}", type );
    }

    public static class ActionPayload {
        private String type;
        private String fromPeerId;
        private String userNickName;

        // Getter/Setter
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getFromPeerId() { return fromPeerId; }
        public void setFromPeerId(String fromPeerId) { this.fromPeerId = fromPeerId; }

        public String getUserNickName() { return userNickName; }
        public void setUserNickName(String userNickName) { this.userNickName = userNickName; }
    }


}
