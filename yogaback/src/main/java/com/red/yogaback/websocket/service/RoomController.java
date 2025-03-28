// RoomController.java
package com.red.yogaback.websocket.service;

import com.red.yogaback.websocket.dto.RoomData;
import com.red.yogaback.websocket.dto.RoomJoinMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class RoomController {

    // 방 정보를 메모리 저장소에 보관 (실제 서비스에서는 DB나 캐시를 사용할 수 있습니다)
    private Map<String, List<String>> roomParticipants = new HashMap<>();

    private final SimpMessagingTemplate messagingTemplate;

    public RoomController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * 클라이언트가 /app/createRoom로 메시지를 보내면
     * 해당 방 정보를 처리한 후, /topic/roomCreated로 결과를 브로드캐스트합니다.
     */
    @MessageMapping("/createRoom")
    @SendTo("/topic/roomCreated")
    public RoomData createRoom(RoomData roomData) {
        // 예를 들어, roomId는 클라이언트에서 생성하거나, 여기서 UUID를 생성할 수 있음
        roomParticipants.put(roomData.getRoomId(), new ArrayList<>());
        // 방 생성 완료 메시지를 /topic/roomCreated로 전송하여, 해당 채널을 구독중인 모든 클라이언트가 알 수 있게 함
        return roomData;
    }

    /**
     * 클라이언트가 /app/joinRoom으로 메시지를 보내면,
     * 해당 방에 사용자를 추가하고, 참가자 목록을 업데이트하여 /topic/room/{roomId}로 브로드캐스트합니다.
     */
    @MessageMapping("/joinRoom")
    public void joinRoom(RoomJoinMessage joinMessage) {
        List<String> participants = roomParticipants.get(joinMessage.getRoomId());
        if (participants != null && participants.size() < 10) { // 예시로 최대 참가자 수 제한 10명
            participants.add(joinMessage.getUserId());
            // /topic/room/{roomId}를 구독중인 클라이언트들에게 참가자 업데이트 정보를 보냄
            messagingTemplate.convertAndSend("/topic/room/" + joinMessage.getRoomId(), participants);
        } else {
            // 방이 없거나 가득 찼으면 에러 메시지를 보낼 수 있음 (추가 구현 필요)
            messagingTemplate.convertAndSendToUser(joinMessage.getUserId(), "/queue/errors", "방이 가득 찼습니다.");
        }
    }
}
