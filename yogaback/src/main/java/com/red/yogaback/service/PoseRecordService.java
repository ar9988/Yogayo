package com.red.yogaback.service;

import com.red.yogaback.dto.request.PoseRecordRequest;
import com.red.yogaback.dto.respond.PoseDetailHistoryRes;
import com.red.yogaback.dto.respond.PoseDetailHistoryRes.HistoryItem;
import com.red.yogaback.dto.respond.PoseHistorySummaryRes;
import com.red.yogaback.dto.respond.PoseRecordRes;
import com.red.yogaback.model.Pose;
import com.red.yogaback.model.PoseRecord;
import com.red.yogaback.model.Room;
import com.red.yogaback.model.User;
import com.red.yogaback.model.UserRecord;
import com.red.yogaback.repository.PoseRecordRepository;
import com.red.yogaback.repository.PoseRepository;
import com.red.yogaback.repository.RoomRecordRepository; // 사용되지 않음
import com.red.yogaback.repository.UserRecordRepository;
import com.red.yogaback.repository.UserRepository;
import com.red.yogaback.repository.RoomRepository;
import com.red.yogaback.security.SecurityUtil;
import com.red.yogaback.service.S3FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PoseRecordService {

    private final PoseRecordRepository poseRecordRepository;
    private final PoseRepository poseRepository;
    // RoomRecordRepository는 더 이상 사용하지 않음
    private final UserRepository userRepository;
    private final UserRecordRepository userRecordRepository;
    private final RoomRepository roomRepository; // 새로 주입받음
    private final S3FileStorageService s3FileStorageService;
    private final BadgeService badgeService;

    /**
     * [POST] /api/yoga/history/{poseId}
     * - 새 요가 포즈 기록을 생성하고 관련 UserRecord의 운동 날짜 필드를 업데이트합니다.
     *
     * 변경사항:
     * 1. 클라이언트가 전달한 PoseRecordRequest 내 roomId 값을 먼저 확인하여,
     *    값이 있으면 해당 room을 조회해 연결하고, 없으면 (또는 null이면) room 필드는 null로 설정합니다.
     * 2. room이 null인 경우 솔로 모드로 간주하여 ranking은 강제로 null로 처리합니다.
     *
     * @param poseId  포즈 ID
     * @param request PoseRecord 요청 DTO (accuracy, ranking, poseTime, roomId)
     * @param recordImg 업로드된 이미지 파일 (선택)
     * @return 생성된 PoseRecord 엔티티
     */
    public PoseRecord createPoseRecord(Long poseId, PoseRecordRequest request, MultipartFile recordImg) {
        Long userId = SecurityUtil.getCurrentMemberId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다. userId=" + userId));

        Pose pose = poseRepository.findById(poseId)
                .orElseThrow(() -> new RuntimeException("해당 포즈를 찾을 수 없습니다. poseId=" + poseId));

        // 요청으로 전달된 roomId 값 사용 (없거나 null이면 solo 모드로 처리)
        Room room = null;
        if (request.getRoomId() != null) {
            room = roomRepository.findById(request.getRoomId())
                    .orElseThrow(() -> new RuntimeException("Room not found with id: " + request.getRoomId()));
        }

        // 솔로 모드일 경우 ranking은 무조건 null 처리
        Integer ranking = (room == null) ? null : request.getRanking();

        String recordImgUrl = null;
        if (recordImg != null && !recordImg.isEmpty()) {
            recordImgUrl = s3FileStorageService.storeFile(recordImg);
        }

        PoseRecord poseRecord = PoseRecord.builder()
                .user(user)
                .room(room)  // room이 null이면 솔로 모드임
                .pose(pose)
                .accuracy(request.getAccuracy())
                .ranking(ranking)
                .poseTime(request.getPoseTime())
                .recordImg(recordImgUrl)
                .createdAt(System.currentTimeMillis())
                .build();
        PoseRecord savedPoseRecord = poseRecordRepository.save(poseRecord);

        // 새 PoseRecord의 생성 시각을 LocalDate로 변환하여 운동 기록 날짜로 사용
        Instant instant = Instant.ofEpochMilli(savedPoseRecord.getCreatedAt());
        LocalDate newExerciseDate = instant.atZone(ZoneId.systemDefault()).toLocalDate();

        // UserRecord 업데이트 (운동 일자 관련 로직)
        UserRecord userRecord = userRecordRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("UserRecord not found for userId=" + userId));

        if (userRecord.getCurrentExerciseDate() == null) {
            userRecord.setCurrentExerciseDate(newExerciseDate);
            userRecord.setExDays(1L);
            userRecord.setExConDays(1L);
        } else if (userRecord.getCurrentExerciseDate().equals(newExerciseDate)) {
            // 이미 오늘 운동 기록이 있다면 아무 작업도 하지 않습니다.
        } else {
            userRecord.setPreviousExerciseDate(userRecord.getCurrentExerciseDate());
            userRecord.setCurrentExerciseDate(newExerciseDate);
            userRecord.setExDays(userRecord.getExDays() + 1);
            if (userRecord.getPreviousExerciseDate() != null &&
                    userRecord.getPreviousExerciseDate().plusDays(1).equals(newExerciseDate)) {
                userRecord.setExConDays(userRecord.getExConDays() + 1);
            } else {
                userRecord.setExConDays(1L);
            }
        }
        userRecordRepository.save(userRecord);

        // 배지 업데이트 (필요 시)
        badgeService.updateUserRecordAndAssignBadges(user);

        return savedPoseRecord;
    }

    // 아래의 getAllPoseRecordsSummary()와 getPoseDetailHistory() 메서드는 기존 코드와 동일합니다.
    public List<PoseHistorySummaryRes> getAllPoseRecordsSummary() {
        Long userId = SecurityUtil.getCurrentMemberId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다. userId=" + userId));

        List<Pose> allPoses = poseRepository.findAll(Sort.by("poseId").ascending());
        List<PoseRecord> userRecords = poseRecordRepository.findByUser(user);

        // poseId를 key로 그룹화
        Map<Long, List<PoseRecord>> recordMap = userRecords.stream()
                .collect(Collectors.groupingBy(r -> r.getPose().getPoseId()));

        List<PoseHistorySummaryRes> result = new ArrayList<>();
        for (Pose pose : allPoses) {
            List<PoseRecord> recordsForPose = recordMap.getOrDefault(pose.getPoseId(), Collections.emptyList());

            float bestAccuracy = 0f;
            float bestTime = 0f;

            if (!recordsForPose.isEmpty()) {
                bestAccuracy = recordsForPose.stream()
                        .filter(r -> r.getAccuracy() != null)
                        .map(PoseRecord::getAccuracy)
                        .max(Float::compareTo)
                        .orElse(0f);

                bestTime = recordsForPose.stream()
                        .filter(r -> r.getPoseTime() != null)
                        .map(PoseRecord::getPoseTime)
                        .max(Float::compareTo)
                        .orElse(0f);
            }

            PoseHistorySummaryRes summary = PoseHistorySummaryRes.builder()
                    .poseId(pose.getPoseId())
                    .poseName(pose.getPoseName())
                    .poseImg(pose.getPoseImg())
                    .bestAccuracy(bestAccuracy)
                    .bestTime(bestTime)
                    .build();

            result.add(summary);
        }
        return result;
    }

    public PoseDetailHistoryRes getPoseDetailHistory(Long poseId) {
        Long userId = SecurityUtil.getCurrentMemberId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다. userId=" + userId));

        Pose pose = poseRepository.findById(poseId)
                .orElseThrow(() -> new RuntimeException("해당 포즈를 찾을 수 없습니다. poseId=" + poseId));

        List<PoseRecord> recordsForPose = poseRecordRepository.findByUserAndPose_PoseIdOrderByCreatedAtDesc(user, poseId);

        float bestAccuracy = 0f;
        float bestTime = 0f;
        int winCount = 0;

        if (!recordsForPose.isEmpty()) {
            bestAccuracy = recordsForPose.stream()
                    .filter(r -> r.getAccuracy() != null)
                    .map(PoseRecord::getAccuracy)
                    .max(Float::compareTo)
                    .orElse(0f);

            bestTime = recordsForPose.stream()
                    .filter(r -> r.getPoseTime() != null)
                    .map(PoseRecord::getPoseTime)
                    .max(Float::compareTo)
                    .orElse(0f);

            winCount = (int) recordsForPose.stream()
                    .filter(r -> r.getRanking() != null && r.getRanking() == 1)
                    .count();
        }

        List<HistoryItem> histories = recordsForPose.stream()
                .map(r -> HistoryItem.builder()
                        .historyId(r.getPoseRecordId())
                        .userId(r.getUser().getUserId())
                        .accuracy(r.getAccuracy())
                        .ranking(r.getRanking())
                        .poseTime(r.getPoseTime())
                        .recordImg(r.getRecordImg())
                        .createdAt(r.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return PoseDetailHistoryRes.builder()
                .poseId(pose.getPoseId())
                .poseName(pose.getPoseName())
                .poseImg(pose.getPoseImg())
                .bestAccuracy(bestAccuracy)
                .bestTime(bestTime)
                .winCount(winCount)
                .histories(histories)
                .build();
    }
}
