package com.red.yogaback.websocket.service;

import com.red.yogaback.model.Room;
import com.red.yogaback.repository.RoomRepository;
import com.red.yogaback.service.RoomService;
import com.red.yogaback.service.SseEmitterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SocketRoomService {

    private final RoomRepository roomRepository;
    private final SseEmitterService sseEmitterService;
    private final RoomService roomService;
    private static final Logger logger = LoggerFactory.getLogger(SocketRoomService.class);
    
    /**
     * 문자열 형태의 roomId를 Long으로 변환하여 Room 엔티티를 조회합니다.
     *
     * 동작:
     *  - NumberFormatException이 발생하면 null을 반환합니다.
     *
     * 개선방향:
     *  - 잘못된 ID 형식에 대해 로깅 또는 커스텀 예외를 던져 호출자에게 원인을 명확히 알릴 수 있습니다.
     *  - Optional<Room>을 반환하도록 변경하여 null 체크를 강제할 수 있습니다.
     */
    public Room getRoom(String roomIdStr) {
        try {
            Long roomId = Long.valueOf(roomIdStr);
            return roomRepository.findById(roomId).orElse(null);
        } catch (NumberFormatException e) {
            // Improvement: 잘못된 ID 형식에 대한 로깅 추가 고려
            return null;
        }
    }

    /**
     * 사용자가 방에 입장할 때 DB의 roomCount를 증가시킵니다.
     * 방 생성 시 roomCount는 1로 설정되어 있다고 가정합니다.
     *
     * 개선방향:
     *  - 동시성 이슈 방지를 위해 데이터베이스 레벨의 증가 쿼리(예: @Modifying 쿼리)를 활용할 수 있습니다.
     *  - roomCount 변경 시 이벤트 발행(예: ApplicationEventPublisher)으로 다른 컴포넌트에 알릴 수 있습니다.
     */
    @Transactional
    public void addParticipant(String roomIdStr) {
        logger.debug("Adding participant to room: {}", roomIdStr);
        Room room = getRoom(roomIdStr);
        if (room != null) {
            int currentCount = room.getRoomCount();
            logger.debug("Current room count: {}", currentCount);
            room.setRoomCount(currentCount + 1);
            roomRepository.save(room);
            sseEmitterService.notifyRoomUpdate(roomService.getAllRooms(""));
            log.info("방 들어옴 room : {}",room);
            logger.debug("Updated room count to: {}", room.getRoomCount());
        } else {
            logger.warn("Room not found: {}", roomIdStr);
        }
    }

    /**
     * 사용자가 방에서 퇴장할 때 DB의 roomCount를 감소시키고,
     * 만약 0이 되면 roomState를 0으로 변경합니다.
     *
     * 개선방향:
     *  - removeParticipant 호출 시에도 동시성 증가/감소 처리를 고려해야 합니다.
     *  - roomState를 enum 타입으로 관리하면 가독성과 안정성이 높아집니다.
     *  - 삭제 후 roomCount가 0일 때 방을 아예 삭제하거나 아카이브하는 로직을 추가할 수 있습니다.
     */
    @Transactional
    public void removeParticipant(String roomIdStr) {
        logger.debug("Removing participant from room: {}", roomIdStr);
        Room room = getRoom(roomIdStr);
        if (room != null) {
            int currentCount = room.getRoomCount();
            logger.debug("Current room count: {}", currentCount);
            if (currentCount > 0) {
                room.setRoomCount(currentCount - 1);
                if (room.getRoomCount() == 0) {
                    // 인원이 0이면 roomState를 0으로 업데이트
                    room.setRoomState(0L);
                }
                roomRepository.save(room);
                sseEmitterService.notifyRoomUpdate(roomService.getAllRooms(""));

                log.info("방 나가기 room : {}",room);
                logger.debug("Updated room count to: {}", room.getRoomCount());
            } else {
                logger.warn("Room count is already 0 or negative: {}", currentCount);
            }
        } else {
            logger.warn("Room not found: {}", roomIdStr);
        }
    }
}
