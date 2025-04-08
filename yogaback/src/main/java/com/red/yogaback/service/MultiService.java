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
     * 엔드포인트1: roomId에 해당하는 방의 코스 내 모든 자세에 대해,
     * 각 자세의 poseTime이 가장 긴 기록의 recordImg와 자세 이름, room_order_index를 반환
     */
    public List<RoomCoursePoseMaxImageDTO> getMaxImageDTOs(Long roomId) {
        // 해당 room의 RoomCoursePose 목록 조회
        List<RoomCoursePose> coursePoses = roomCoursePoseRepository.findByRoom_RoomId(roomId);
        // 각 coursePose별로 최대 poseTime을 가진 record의 이미지 URL을 찾음
        List<RoomCoursePoseMaxImageDTO> dtoList = new ArrayList<>();

        for (RoomCoursePose coursePose : coursePoses) {
            Long poseId = coursePose.getPose().getPoseId();
            // roomId는 RoomRecord 내에 있으므로, query PoseRecordRepository에서 해당 roomId와 poseId에 해당하는 기록 조회
            List<PoseRecord> records = poseRecordRepository.findByRoomIdAndPoseIdOrderByPoseTimeDesc(roomId, poseId);
            String maxImageUrl = "";
            if (!records.isEmpty()) {
                // 첫번째가 longest poseTime 기록
                maxImageUrl = records.get(0).getRecordImg();
            }
            RoomCoursePoseMaxImageDTO dto = RoomCoursePoseMaxImageDTO.builder()
                    .poseName(coursePose.getPose().getPoseName())
                    .poseUrl(maxImageUrl)
                    .roomOrderIndex(coursePose.getRoomOrderIndex())
                    .build();
            dtoList.add(dto);
        }
        // 필요시 roomOrderIndex 순으로 정렬
        return dtoList.stream()
                .sorted(Comparator.comparingInt(RoomCoursePoseMaxImageDTO::getRoomOrderIndex))
                .collect(Collectors.toList());
    }

    /**
     * 엔드포인트2: roomId와 room_order_index(순서)에 해당하는 자세의 모든 PoseRecord를 조회하여,
     * 사용자 이름, 사진 URL, poseTime, accuracy, ranking 정보를 반환.
     * poseRecords는 poseTime 내림차순으로 정렬
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
        // DTO로 변환
        return records.stream()
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
