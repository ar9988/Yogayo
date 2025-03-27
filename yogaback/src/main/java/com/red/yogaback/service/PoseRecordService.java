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
import com.red.yogaback.service.S3FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PoseRecordService {

    private final PoseRecordRepository poseRecordRepository;
    private final PoseRepository poseRepository;
    private final RoomRecordRepository roomRecordRepository;
    private final UserRepository userRepository;
    private final S3FileStorageService s3FileStorageService;

    /**
     * POST: 새 요가 기록 생성
     * - poseId (경로)
     * - userId (JWT)
     * - roomRecordId, ranking은 요청 바디에서 null 가능
     * - recordImg는 MultipartFile로 받아 S3에 업로드 후 URL을 저장
     */
    public PoseRecord createPoseRecord(Long poseId, PoseRecordRequest request, MultipartFile recordImg) {
        // 1) JWT에서 userId 추출
        Long userId = SecurityUtil.getCurrentMemberId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

        // 2) poseId로 Pose 엔티티 조회
        Pose pose = poseRepository.findById(poseId)
                .orElseThrow(() -> new RuntimeException("해당 포즈를 찾을 수 없습니다. poseId=" + poseId));

        // 3) roomRecordId가 존재하면 RoomRecord 조회, 없으면 null 처리
        RoomRecord roomRecord = null;
        if (request.getRoomRecordId() != null) {
            roomRecord = roomRecordRepository.findById(request.getRoomRecordId())
                    .orElseThrow(() -> new RuntimeException("해당 roomRecordId의 방 기록을 찾을 수 없습니다."));
        }

        // 4) recordImg 파일이 있다면 S3로 업로드하여 URL을 획득, 없으면 null로 처리
        String recordImgUrl = null;
        if (recordImg != null && !recordImg.isEmpty()) {
            recordImgUrl = s3FileStorageService.storeFile(recordImg);
        }

        // 5) 빌더 패턴을 이용해 PoseRecord 엔티티 생성
        PoseRecord poseRecord = PoseRecord.builder()
                .user(user)
                .pose(pose)
                .roomRecord(roomRecord)
                .accuracy(request.getAccuracy())
                .ranking(request.getRanking())
                .poseTime(request.getPoseTime())
                .recordImg(recordImgUrl)
                .createdAt(System.currentTimeMillis())
                .build();

        // 6) DB에 저장 후 반환
        return poseRecordRepository.save(poseRecord);
    }

    /**
     * GET: 특정 포즈에 대한 기록 목록 조회 (현재 사용자 기준)
     */
    public List<PoseRecord> getPoseRecordsByPoseId(Long poseId) {
        Long userId = SecurityUtil.getCurrentMemberId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

        Pose pose = poseRepository.findById(poseId)
                .orElseThrow(() -> new RuntimeException("해당 포즈를 찾을 수 없습니다. poseId=" + poseId));

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
