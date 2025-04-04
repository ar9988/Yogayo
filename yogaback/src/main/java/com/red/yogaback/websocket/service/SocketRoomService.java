package com.red.yogaback.websocket.service;

import com.red.yogaback.model.Room;
import com.red.yogaback.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SocketRoomService {

    private final RoomRepository roomRepository;

    @Autowired
    public SocketRoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

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
            // roomCount가 null이면 기본값 1로 간주
            Long currentCount = room.getRoomCount() != null ? room.getRoomCount() : 1L;
            room.setRoomCount(currentCount + 1);
            roomRepository.save(room);
        }
    }

    // 사용자가 방에서 퇴장할 때 DB의 roomCount를 감소시키고, 만약 0이 되면 roomState를 0으로 변경합니다.
    public void removeParticipant(String roomIdStr) {
        Room room = getRoom(roomIdStr);
        if (room != null) {
            // roomCount가 null이면 기본값 1로 간주
            Long currentCount = room.getRoomCount() != null ? room.getRoomCount() : 1L;
            if (currentCount > 0) {
                room.setRoomCount(currentCount - 1);
                if (room.getRoomCount() == 0) {
                    // 인원이 0이면 roomState를 0으로 업데이트 (예: 0은 비활성 상태)
                    room.setRoomState(0L);
                }
                roomRepository.save(room);
            }
        }
    }
}
