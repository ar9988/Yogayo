package com.red.yogaback.controller;

import com.red.yogaback.dto.request.RoomRecordRequest;
import com.red.yogaback.dto.respond.RoomRecordResponse;
import com.red.yogaback.service.RoomRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/room-record")
@Tag(name = "Room Record API", description = "최종 방 기록(최종 등수 및 점수) 관리 API")
@RequiredArgsConstructor
public class RoomRecordController {

    private final RoomRecordService roomRecordService;

    /**
     * [POST] /api/room-record
     * 최종 기록(최종 등수와 최종 점수)을 저장합니다.
     *
     * 클라이언트는 해당 엔드포인트에 최종 기록 데이터를 보내야 합니다.
     */
    @PostMapping
    @Operation(summary = "최종 방 기록 저장", description = "클라이언트에서 전달받은 최종 등수와 점수를 RoomRecord에 저장합니다.")
    public ResponseEntity<RoomRecordResponse> saveRoomRecord(@RequestBody RoomRecordRequest request) {
        RoomRecordResponse response = roomRecordService.saveFinalRoomRecord(request);
        return ResponseEntity.ok(response);
    }
}
