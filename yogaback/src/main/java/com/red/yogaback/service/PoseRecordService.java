package com.red.yogaback.service;

import com.red.yogaback.dto.request.PoseRecordRequest;
import com.red.yogaback.model.Pose;
import com.red.yogaback.model.PoseRecord;
import com.red.yogaback.model.RoomRecord;
import com.red.yogaback.model.User;
import com.red.yogaback.repository.PoseRecordRepository;
import com.red.yogaback.repository.PoseRepository;
import com.red.yogaback.repository.RoomRecordRepository;
import com.red.yogaback.repository.UserRepository;
import com.red.yogaback.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PoseRecordService {

    private final PoseRecordRepository poseRecordRepository;
    private final PoseRepository poseRepository;
    private final RoomRecordRepository roomRecordRepository;
    private final UserRepository userRepository;

    /**
     * POST: 새 요가 기록 생성
     * - poseId (경로)
     * - userId (JWT)
     * - roomRecordId, ranking은 null 가능
     */
    public PoseRecord createPoseRecord(Long poseId, PoseRecordRequest request) {
        // 1) JWT에서 userId를 추출
        Long userId = SecurityUtil.getCurrentMemberId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

        // 2) poseId로 Pose 엔티티 조회
        Pose pose = poseRepository.findById(poseId)
                .orElseThrow(() -> new RuntimeException("해당 포즈를 찾을 수 없습니다. poseId=" + poseId));

        // 3) roomRecordId가 존재한다면 RoomRecord 조회, 없다면 null
        RoomRecord roomRecord = null;
        if (request.getRoomRecordId() != null) {
            roomRecord = roomRecordRepository.findById(request.getRoomRecordId())
                    .orElseThrow(() -> new RuntimeException("해당 roomRecordId의 방 기록을 찾을 수 없습니다."));
        }

        // 4) 빌더로 PoseRecord 생성
        PoseRecord poseRecord = PoseRecord.builder()
                .user(user)
                .pose(pose)
                .roomRecord(roomRecord)
                .accuracy(request.getAccuracy())
                .ranking(request.getRanking())
                .poseTime(request.getPoseTime())
                .recordImg(request.getRecordImg())
                .createdAt(System.currentTimeMillis())
                .build();

        // 5) DB 저장
        return poseRecordRepository.save(poseRecord);
    }

    /**
     * GET: 특정 포즈에 대한 기록 목록 조회 (현재 사용자 기준)
     * - poseId (경로)
     * - userId (JWT)
     */
    public List<PoseRecord> getPoseRecordsByPoseId(Long poseId) {
        // 1) JWT에서 userId 추출
        Long userId = SecurityUtil.getCurrentMemberId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

        // 2) poseId로 Pose 엔티티 조회 (존재 여부 확인)
        Pose pose = poseRepository.findById(poseId)
                .orElseThrow(() -> new RuntimeException("해당 포즈를 찾을 수 없습니다. poseId=" + poseId));

        // 3) 사용자 전체 기록 중에서 poseId가 일치하는 것만 필터
        //    (또는 PoseRecordRepository에 findByUserAndPose(...) 메서드를 추가할 수도 있음)
        return poseRecordRepository.findByUser(user).stream()
                .filter(record -> record.getPose().getPoseId().equals(poseId))
                .toList();
    }

    /**
     * GET: 현재 사용자 기준, 모든 요가 기록 조회
     */
    public List<PoseRecord> getAllPoseRecords() {
        Long userId = SecurityUtil.getCurrentMemberId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

        return poseRecordRepository.findByUser(user);
    }
}
