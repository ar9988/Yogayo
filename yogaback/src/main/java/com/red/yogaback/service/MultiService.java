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
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MultiService {

    private final RoomCoursePoseRepository roomCoursePoseRepository;
    private final PoseRecordRepository poseRecordRepository;

    /**
     * 엔드포인트1: roomId에 해당하는 방의 코스 내 각 자세에서,
     * 각 자세의 poseTime이 가장 긴 기록의 recordImg와 자세 이름, room_order_index를 반환합니다.
     */
    public List<RoomCoursePoseMaxImageDTO> getMaxImageDTOs(Long roomId) {
        // 해당 room의 RoomCoursePose 목록 조회
        List<RoomCoursePose> coursePoses = roomCoursePoseRepository.findByRoom_RoomId(roomId);
        List<RoomCoursePoseMaxImageDTO> dtoList = new ArrayList<>();

        for (RoomCoursePose coursePose : coursePoses) {
            Long poseId = coursePose.getPose().getPoseId();
            // roomId와 poseId 조건으로 PoseRecord 조회
            List<PoseRecord> records = poseRecordRepository.findByRoomIdAndPoseIdOrderByPoseTimeDesc(roomId, poseId);
            String maxImageUrl = "";
            if (!records.isEmpty()) {
                // 첫 번째가 가장 긴 poseTime 기록 (이미 내림차순 정렬되어 있음)
                maxImageUrl = records.get(0).getRecordImg();
            }
            RoomCoursePoseMaxImageDTO dto = RoomCoursePoseMaxImageDTO.builder()
                    .poseName(coursePose.getPose().getPoseName())
                    .poseUrl(maxImageUrl)
                    .roomOrderIndex(coursePose.getRoomOrderIndex())
                    .build();
            dtoList.add(dto);
        }
        // room_order_index 순으로 정렬하여 반환
        return dtoList.stream()
                .sorted(Comparator.comparingInt(RoomCoursePoseMaxImageDTO::getRoomOrderIndex))
                .collect(Collectors.toList());
    }

    /**
     * 엔드포인트2: roomId와 roomOrderIndex(순서)에 해당하는 자세의 모든 PoseRecord를 조회하여,
     * 사용자별로 대표적인(예를 들어 poseTime 기준 최대인) 기록을 반환합니다.
     * 반환되는 각 DTO에는 userName, poseUrl, poseTime, accuracy, ranking 정보가 담깁니다.
     */
    public List<RoomCoursePoseRecordDTO> getPoseRecordDTOs(Long roomId, int roomOrderIndex) {
        // 해당 roomId에 속하는 RoomCoursePose 중 지정한 roomOrderIndex를 가진 객체를 찾음
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

        // roomId와 poseId 조건으로 모든 PoseRecord 조회
        List<PoseRecord> records = poseRecordRepository.findByRoomIdAndPoseIdOrderByPoseTimeDesc(roomId, poseId);

        // 사용자별로 대표 기록을 선택한다.
        // 예시로, 동일 사용자의 여러 기록 중 poseTime이 최대인 기록을 선택.
        Map<Long, PoseRecord> bestRecordByUser = records.stream()
                .collect(Collectors.toMap(
                        record -> record.getUser().getUserId(),
                        record -> record,
                        (record1, record2) -> record1.getPoseTime() >= record2.getPoseTime() ? record1 : record2
                ));

        // DTO 변환: 사용자별 대표 기록만을 반환
        return bestRecordByUser.values().stream()
                .map(record ->
                        RoomCoursePoseRecordDTO.builder()
                                .userName(record.getUser().getUserName())
                                .poseUrl(record.getRecordImg())
                                .poseTime(record.getPoseTime())
                                .accuracy(record.getAccuracy())
                                .ranking(record.getRanking())
                                .build()
                )
                .collect(Collectors.toList());
    }
}
