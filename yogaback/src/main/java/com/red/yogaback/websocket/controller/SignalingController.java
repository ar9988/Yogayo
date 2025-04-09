package com.red.yogaback.websocket.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.red.yogaback.model.Room;
import com.red.yogaback.repository.RoomRepository;
import com.red.yogaback.websocket.dto.RoomActionMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Controller
public class SignalingController {

    private static final Logger logger = LoggerFactory.getLogger(SignalingController.class);

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private RoomRepository roomRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 클라이언트가 "/app/room/{roomId}"로 메시지를 전송하면
     * 1) payload를 "/topic/room/{roomId}"로 브로드캐스트
     * 2) game_state & state==0 인 경우 DB의 roomState를 0으로 변경
     */
    @MessageMapping("/room/{roomId}")
    @Transactional
    public void broadcastRoomMessage(@DestinationVariable String roomId,
                                     @Payload RoomActionMessage actionMessage,
                                     StompHeaderAccessor headerAccessor) {
        Object payload = actionMessage.getPayload();
        String sessionId = headerAccessor.getSessionId();

        // 1) 받은 payload 그대로 브로드캐스트
        messagingTemplate.convertAndSend("/topic/room/" + roomId, payload);
        logger.info("Broadcasted to /topic/room/{} from session {}: {}", roomId, sessionId, payload);

        // 2) payload를 Map<String,Object> 로 변환 (POJO, Map, JSON 문자열 모두 처리)
        Map<String,Object> map;
        try {
            map = objectMapper.convertValue(
                    payload,
                    new TypeReference<Map<String,Object>>() {}
            );
        } catch (IllegalArgumentException e) {
            logger.warn("Payload를 Map으로 변환 실패, state 체크 생략: {}", payload, e);
            return;
        }

        // 3) type, state 추출
        String type = (String) map.get("type");
        Integer state = null;
        Object stateObj = map.get("state");
        if (stateObj instanceof Number) {
            state = ((Number) stateObj).intValue();
        } else if (stateObj instanceof String) {
            try {
                state = Integer.parseInt((String) stateObj);
            } catch (NumberFormatException ignored) {}
        }

        // 4) game_state && state == 0 이면 DB 업데이트
        if ("game_state".equals(type) && state != null && state == 0) {
            Long rid = Long.valueOf(roomId);
            roomRepository.findById(rid).ifPresent(room -> {
                room.setRoomState(0L);
                roomRepository.save(room);
                logger.info("Room {} 의 roomState를 0으로 변경했습니다.", roomId);
            });
        }
    }
}
