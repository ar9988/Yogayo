package com.red.yogaback.websocket.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.Collection;

@Service
public class RoundManagerService {

    @Autowired
    private SocketRoomService socketRoomService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // 5초마다 라운드 상태를 확인 (필요에 따라 주기를 조절)
    @Scheduled(fixedDelay = 5000)
    public void manageRounds() {
        Collection<Room> rooms = socketRoomService.getAllRooms();
        for (Room room : rooms) {
            if (room.isCourseStarted()) {
                if (room.isRoundEnded()) {
                    // 현재 라운드 종료 알림 전송
                    messagingTemplate.convertAndSend("/topic/room/" + room.getRoomId() + "/roundEnd", 
                        "Round " + room.getCurrentRound() + " ended.");
                    
                    // 다음 라운드가 존재하면 시작, 없으면 코스 종료
                    if (room.hasNextRound()) {
                        room.nextRound();
                        messagingTemplate.convertAndSend("/topic/room/" + room.getRoomId() + "/roundStart", 
                            "Round " + room.getCurrentRound() + " started.");
                    } else {
                        messagingTemplate.convertAndSend("/topic/room/" + room.getRoomId() + "/courseEnd", 
                            "Course completed.");
                        room.setCourseStarted(false);
                    }
                }
            }
        }
    }
}
