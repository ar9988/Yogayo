package com.red.yogaback.service;

import com.red.yogaback.dto.request.PoseRecordRequest;
import com.red.yogaback.dto.respond.PoseDetailHistoryRes;
import com.red.yogaback.dto.respond.PoseDetailHistoryRes.HistoryItem;
import com.red.yogaback.dto.respond.PoseHistorySummaryRes;
import com.red.yogaback.dto.respond.PoseRecordRes;
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
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PoseRecordService {

    private final PoseRecordRepository poseRecordRepository;
    private final PoseRepository poseRepository;
    private final RoomRecordRepository roomRecordRepository;
    private final UserRepository userRepository;
    private final S3FileStorageService s3FileStorageService;
    private final BadgeService badgeService;

    /**
     * [POST] /api/yoga/history/{poseId}
     * - 새 요가 포즈 기록 생성
     */
    public PoseRecord createPoseRecord(Long poseId, PoseRecordRequest request, MultipartFile recordImg) {
        Long userId = SecurityUtil.getCurrentMemberId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다. userId=" + userId));

        Pose pose = poseRepository.findById(poseId)
                .orElseThrow(() -> new RuntimeException("해당 포즈를 찾을 수 없습니다. poseId=" + poseId));

        RoomRecord roomRecord = null;
        if (request.getRoomRecordId() != null) {
            roomRecord = roomRecordRepository.findById(request.getRoomRecordId())
                    .orElseThrow(() -> new RuntimeException("해당 roomRecordId의 방 기록을 찾을 수 없습니다."));
        }

        String recordImgUrl = null;
        if (recordImg != null && !recordImg.isEmpty()) {
            recordImgUrl = s3FileStorageService.storeFile(recordImg);
        }

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
        PoseRecord savedPoseRecord = poseRecordRepository.save(poseRecord);
        badgeService.updateUserRecordAndAssignBadges(user);
        return savedPoseRecord;
    }

    /**
     * [GET] /api/yoga/history
     * - 전체 요가 포즈 기록 조회 (사용자별)
     * - 포즈 목록을 DB에서 정렬(작은 poseId부터 큰 poseId 순)하여 가져오고, 각 포즈에 대해 bestAccuracy, bestTime 계산
     */
    public List<PoseHistorySummaryRes> getAllPoseRecordsSummary() {
        Long userId = SecurityUtil.getCurrentMemberId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다. userId=" + userId));

        // 전체 Pose 목록을 DB에서 poseId 오름차순 정렬하여 조회
        List<Pose> allPoses = poseRepository.findAll(Sort.by("poseId").ascending());

        // 사용자 모든 기록 조회 (한 번에 가져와서 Pose별로 그룹화)
        List<PoseRecord> userRecords = poseRecordRepository.findByUser(user);

        // poseId를 key로, 해당 PoseRecord 목록을 그룹화
        Map<Long, List<PoseRecord>> recordMap = userRecords.stream()
                .collect(Collectors.groupingBy(r -> r.getPose().getPoseId()));

        // 각 pose에 대해 bestAccuracy, bestTime 계산
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

    /**
     * [GET] /api/yoga/history/{poseId}
     * - 특정 요가 포즈 기록 조회
     * - bestAccuracy, bestTime, winCount, histories 배열 (histories는 DB에서 정렬된 createdAt 내림차순)
     */
    public PoseDetailHistoryRes getPoseDetailHistory(Long poseId) {
        Long userId = SecurityUtil.getCurrentMemberId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다. userId=" + userId));

        Pose pose = poseRepository.findById(poseId)
                .orElseThrow(() -> new RuntimeException("해당 포즈를 찾을 수 없습니다. poseId=" + poseId));

        // DB에서 바로 createdAt 내림차순으로 정렬된 해당 포즈의 기록 조회
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

        // histories 배열 생성 (DB에서 이미 정렬된 상태)
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
