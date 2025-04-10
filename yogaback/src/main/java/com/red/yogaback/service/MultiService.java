package com.red.yogaback.service;

import com.red.yogaback.dto.respond.RoomCoursePoseMaxImageDTO;
import com.red.yogaback.dto.respond.RoomCoursePoseRecordDTO;
import com.red.yogaback.model.PoseRecord;
import com.red.yogaback.model.RoomCoursePose;
import com.red.yogaback.repository.PoseRecordRepository;
import com.red.yogaback.repository.RoomCoursePoseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MultiService {

    private final RoomCoursePoseRepository roomCoursePoseRepository;
    private final PoseRecordRepository poseRecordRepository;

    /**
     * GET /api/multi/{roomId}
     * 주어진 roomId에 대해 각 자세에서 1등의 poseUrl을 포함하여 반환.
     * 각 {roomOrderIndex} 순으로 배열로 반환합니다.
     */
    public List<RoomCoursePoseMaxImageDTO> getMaxImageDTOs(Long roomId) {
        List<RoomCoursePose> coursePoses = roomCoursePoseRepository.findByRoom_RoomId(roomId);
        List<RoomCoursePoseMaxImageDTO> dtoList = new ArrayList<>();

        for (RoomCoursePose coursePose : coursePoses) {
            Long poseId = coursePose.getPose().getPoseId();
            // 해당 roomId와 poseId에 해당하는 PoseRecord들을 내림차순으로 조회
            List<PoseRecord> records = poseRecordRepository.findByRoomIdAndPoseIdOrderByPoseTimeDesc(roomId, poseId);

            String maxImageUrl = "";
            if (!records.isEmpty()) {
                // ranking 1위인 사람의 포즈 URL만 가져오기
                PoseRecord firstRecord = records.stream()
                        .filter(record -> record.getRanking() != null && record.getRanking() == 1)
                        .findFirst()
                        .orElse(records.get(0)); // ranking 1위가 없으면 첫 번째 기록을 기본값으로 사용

                maxImageUrl = firstRecord.getRecordImg();
            }

            RoomCoursePoseMaxImageDTO dto = RoomCoursePoseMaxImageDTO.builder()
                    .poseName(coursePose.getPose().getPoseName())
                    .poseUrl(maxImageUrl)
                    .roomOrderIndex(coursePose.getRoomOrderIndex())
                    .build();
            dtoList.add(dto);
        }

        // roomOrderIndex 순으로 정렬하여 반환
        return dtoList.stream()
                .sorted(Comparator.comparingInt(RoomCoursePoseMaxImageDTO::getRoomOrderIndex))
                .collect(Collectors.toList());
    }

    /**
     * GET /api/multi/{roomId}/{roomOrderIndex}
     * 주어진 roomId와 room_order_index에 해당하는 자세의 모든 PoseRecord를 조회하여,
     * ranking 순으로 정렬된 사용자 기록을 반환.
     */
    public List<RoomCoursePoseRecordDTO> getPoseRecordDTOs(Long roomId, int roomOrderIndex) {
        // 해당 roomId에 속하는 RoomCoursePose 중 입력된 roomOrderIndex를 가진 객체를 찾음
        Optional<RoomCoursePose> optionalCoursePose = roomCoursePoseRepository
                .findByRoom_RoomId(roomId)
                .stream()
                .filter(rcp -> rcp.getRoomOrderIndex() == roomOrderIndex)
                .findFirst();

        if (!optionalCoursePose.isPresent()) {
            return new ArrayList<>();
        }

        RoomCoursePose coursePose = optionalCoursePose.get();
        Long poseId = coursePose.getPose().getPoseId();
        // 해당 roomId와 poseId에 해당하는 PoseRecord들을 내림차순으로 조회
        List<PoseRecord> records = poseRecordRepository.findByRoomIdAndPoseIdOrderByPoseTimeDesc(roomId, poseId);

        // ranking 순으로 정렬하여 반환 (1위부터)
        // 2명만 반환되도록 제한
        return records.stream()
                .sorted(Comparator.comparingInt(PoseRecord::getRanking))
                .limit(2) // ranking 순으로 상위 2명만 반환
                .map(record -> RoomCoursePoseRecordDTO.builder()
                        .userName(record.getUser().getUserName())
                        .poseUrl(record.getRecordImg())
                        .poseTime(record.getPoseTime())
                        .accuracy(record.getAccuracy())
                        .ranking(record.getRanking())
                        .build())
                .collect(Collectors.toList());
    }
}
