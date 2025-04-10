package com.red.yogaback.service;

import com.red.yogaback.dto.respond.RoomCoursePoseMaxImageDTO;
import com.red.yogaback.dto.respond.RoomCoursePoseRecordDTO;
import com.red.yogaback.model.PoseRecord;
import com.red.yogaback.model.RoomCoursePose;
import com.red.yogaback.repository.PoseRecordRepository;
import com.red.yogaback.repository.RoomCoursePoseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MultiService {

    private final RoomCoursePoseRepository roomCoursePoseRepository;
    private final PoseRecordRepository poseRecordRepository;

    // roomId와 roomOrderIndex에 맞는 가장 긴 poseTime을 가진 레코드의 사진 URL을 반환
    public List<RoomCoursePoseMaxImageDTO> getMaxImageDTOs(Long roomId) {
        List<RoomCoursePose> coursePoses = roomCoursePoseRepository.findByRoom_RoomId(roomId);
        List<RoomCoursePoseMaxImageDTO> dtoList = new ArrayList<>();

        for (RoomCoursePose coursePose : coursePoses) {
            Long poseId = coursePose.getPose().getPoseId();
            List<PoseRecord> records = poseRecordRepository.findByRoomIdAndPoseIdOrderByPoseTimeDesc(roomId, poseId);
            String maxImageUrl = "";
            if (!records.isEmpty()) {
                maxImageUrl = records.get(0).getRecordImg();
            }
            RoomCoursePoseMaxImageDTO dto = RoomCoursePoseMaxImageDTO.builder()
                    .poseName(coursePose.getPose().getPoseName())
                    .poseUrl(maxImageUrl)
                    .roomOrderIndex(coursePose.getRoomOrderIndex())
                    .build();
            dtoList.add(dto);
        }

        return dtoList.stream()
                .sorted(Comparator.comparingInt(RoomCoursePoseMaxImageDTO::getRoomOrderIndex))
                .collect(Collectors.toList());
    }

    // roomOrderIndex와 roomId에 해당하는 자세 기록을 반환
    public List<RoomCoursePoseRecordDTO> getPoseRecordDTOs(Long roomId, int roomOrderIndex) {
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

        List<PoseRecord> records = poseRecordRepository.findByRoomIdAndPoseIdOrderByPoseTimeDesc(roomId, poseId);

        // 각 사용자별로 가장 높은 poseTime을 가진 기록을 선택
        Map<Long, PoseRecord> bestRecordByUser = records.stream()
                .collect(Collectors.toMap(
                        record -> record.getUser().getUserId(),
                        record -> record,
                        (record1, record2) -> record1.getPoseTime() >= record2.getPoseTime() ? record1 : record2
                ));

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
