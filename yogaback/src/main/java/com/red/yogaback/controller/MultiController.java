package com.red.yogaback.controller;

import com.red.yogaback.dto.respond.RoomCoursePoseMaxImageDTO;
import com.red.yogaback.dto.respond.RoomCoursePoseRecordDTO;
import com.red.yogaback.service.MultiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/multi")
@Tag(name = "Multi API", description = "방 코스 관련 추가 정보 제공 API")
@RequiredArgsConstructor
public class MultiController {

    private final MultiService multiService;

    /**
     * GET /api/multi/{roomId}
     * 주어진 roomId에 대해 해당 방의 코스 내 각 자세에서 가장 긴 poseTime을 가진 레코드의 사진 URL 및 자세명을,
     * room_order_index순으로 배열로 반환
     */
    @GetMapping("/{roomId}")
    public ResponseEntity<List<RoomCoursePoseMaxImageDTO>> getRoomCourseMaxImages(@PathVariable Long roomId) {
        List<RoomCoursePoseMaxImageDTO> result = multiService.getMaxImageDTOs(roomId);
        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/multi/{roomId}/{roomOrderIndex}
     * 주어진 roomId와 room_order_index(순서)에 해당하는 자세에 대하여,
     * 그 자세의 모든 PoseRecord(연관된 사용자 기록) 정보를 배열로 반환
     * 각 요소: userName, poseUrl, poseTime, accuracy, ranking
     */
    @GetMapping("/{roomId}/{roomOrderIndex}")
    public ResponseEntity<List<RoomCoursePoseRecordDTO>> getRoomCoursePoseRecords(@PathVariable Long roomId,
                                                                                  @PathVariable int roomOrderIndex) {
        List<RoomCoursePoseRecordDTO> result = multiService.getPoseRecordDTOs(roomId, roomOrderIndex);
        return ResponseEntity.ok(result);
    }
    
}
