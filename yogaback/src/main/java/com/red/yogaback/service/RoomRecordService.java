package com.red.yogaback.service;

import com.red.yogaback.dto.request.RoomRecordRequest;
import com.red.yogaback.dto.respond.RoomRecordResponse;
import com.red.yogaback.model.Room;
import com.red.yogaback.model.RoomRecord;
import com.red.yogaback.model.User;
import com.red.yogaback.repository.RoomRecordRepository;
import com.red.yogaback.repository.RoomRepository;
import com.red.yogaback.repository.UserRepository;
import com.red.yogaback.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoomRecordService {

    private final RoomRecordRepository roomRecordRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    /**
     * 클라이언트로부터 전달받은 최종 방 기록 정보를 기반으로 RoomRecord를 저장합니다.
     *
     * @param request 최종 기록 요청 DTO (roomId, totalRanking, totalScore)
     * @return 저장된 RoomRecord의 결과 DTO
     */
    public RoomRecordResponse saveFinalRoomRecord(RoomRecordRequest request) {
        // 현재 로그인한 사용자 ID를 가져옴
        Long userId = SecurityUtil.getCurrentMemberId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // 요청된 roomId에 해당하는 Room 엔티티 조회
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + request.getRoomId()));

        // 빌더 패턴을 사용하여 RoomRecord 엔티티 생성
        RoomRecord roomRecord = RoomRecord.builder()
                .user(user)
                .room(room)
                .totalRanking(request.getTotalRanking())
                .totalScore(request.getTotalScore())
                .createdAt(System.currentTimeMillis())
                .build();

        // 저장 후 결과 반환
        RoomRecord savedRecord = roomRecordRepository.save(roomRecord);

        return RoomRecordResponse.builder()
                .roomRecordId(savedRecord.getRoomRecordId())
                .userId(user.getUserId())
                .roomId(room.getRoomId())
                .totalRanking(savedRecord.getTotalRanking())
                .totalScore(savedRecord.getTotalScore())
                .createdAt(savedRecord.getCreatedAt())
                .build();
    }
}
