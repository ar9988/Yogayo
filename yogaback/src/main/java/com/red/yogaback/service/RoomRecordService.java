package com.red.yogaback.service;

import com.red.yogaback.dto.request.RoomRecordRequest;
import com.red.yogaback.dto.respond.RoomRecordResponse;
import com.red.yogaback.model.Room;
import com.red.yogaback.model.RoomRecord;
import com.red.yogaback.model.User;
import com.red.yogaback.model.UserRecord;
import com.red.yogaback.repository.RoomRecordRepository;
import com.red.yogaback.repository.RoomRepository;
import com.red.yogaback.repository.UserRecordRepository;
import com.red.yogaback.repository.UserRepository;
import com.red.yogaback.security.SecurityUtil;
import com.red.yogaback.service.BadgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoomRecordService {

    private final RoomRecordRepository roomRecordRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final UserRecordRepository userRecordRepository; // 추가됨
    private final BadgeService badgeService;               // 추가됨

    /**
     * 클라이언트로부터 전달받은 최종 방 기록 정보를 기반으로 RoomRecord를 저장합니다.
     * 최종 기록 저장 시 totalRanking 값이 1이면 해당 사용자의 UserRecord의 roomWin을 1 증가시키고,
     * badgeService를 통해 배지 업데이트를 수행합니다.
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

        // RoomRecord 저장
        RoomRecord savedRecord = roomRecordRepository.save(roomRecord);

        // 최종 기록이 우승인 경우(totalRanking == 1) userRecord의 roomWin 값을 증가시킵니다.
        if (savedRecord.getTotalRanking() != null && savedRecord.getTotalRanking() == 1) {
            UserRecord userRecord = userRecordRepository.findByUser(user)
                    .orElseThrow(() -> new RuntimeException("UserRecord not found for userId=" + userId));
            userRecord.setRoomWin(userRecord.getRoomWin() + 1);
            userRecordRepository.save(userRecord);
        }

        // 배지 업데이트: roomWin 변경 후 뱃지 상태를 다시 검증합니다.
        badgeService.updateUserRecordAndAssignBadges(user);

        // 결과 DTO 반환
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
