package com.red.yogaback.websocket.service;

import com.red.yogaback.model.Room;
import com.red.yogaback.repository.RoomRepository;
import com.red.yogaback.service.RoomService;
import com.red.yogaback.service.SseEmitterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SocketRoomService {

    private final RoomRepository roomRepository;
    private final SseEmitterService sseEmitterService;
    private final RoomService roomService;


    // 문자열 형태의 roomId를 Long으로 변환하여 Room 엔티티를 조회합니다.
    public Room getRoom(String roomIdStr) {
        try {
            Long roomId = Long.valueOf(roomIdStr);
            return roomRepository.findById(roomId).orElse(null);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // 사용자가 방에 입장할 때 DB의 roomCount를 증가시킵니다.
    // 방 생성 시 roomCount는 1로 설정되어 있다고 가정합니다.
    public void addParticipant(String roomIdStr) {
        Room room = getRoom(roomIdStr);
        if (room != null) {
            int currentCount = room.getRoomCount(); // null 체크 불필요
            // 방 생성 시 roomCount가 1로 시작하도록 설정되어 있다면, 추가 입장 시 증가
            room.setRoomCount(currentCount + 1);
            roomRepository.save(room);
            sseEmitterService.notifyRoomUpdate(roomService.getAllRooms(""));
            log.info("방 들어옴 room : {}",room);
        }
    }

    // 사용자가 방에서 퇴장할 때 DB의 roomCount를 감소시키고, 만약 0이 되면 roomState를 0으로 변경합니다.
    public void removeParticipant(String roomIdStr) {
        Room room = getRoom(roomIdStr);
        if (room != null) {
            int currentCount = room.getRoomCount();
            if (currentCount > 0) {
                room.setRoomCount(currentCount - 1);
                if (room.getRoomCount() == 0) {
                    // 인원이 0이면 roomState를 0으로 업데이트
                    room.setRoomState(0L);
                }
                roomRepository.save(room);
                sseEmitterService.notifyRoomUpdate(roomService.getAllRooms(""));

                log.info("방 나가기 room : {}",room);
            }
        }
    }
}
