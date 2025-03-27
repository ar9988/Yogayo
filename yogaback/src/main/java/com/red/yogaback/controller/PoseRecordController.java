package com.red.yogaback.controller;

import com.red.yogaback.dto.request.PoseRecordRequest;
import com.red.yogaback.dto.respond.PoseRecordRes;
import com.red.yogaback.model.PoseRecord;
import com.red.yogaback.service.PoseRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@Tag(name = "요가 기록 API", description = "PoseRecord를 이용한 요가 포즈 기록 관리")
@RequestMapping("/api/yoga/history")
@RequiredArgsConstructor
public class PoseRecordController {

    private final PoseRecordService poseRecordService;

    /**
     * POST /api/yoga/history/{poseId}
     * - 요가 기록 저장
     * - poseId: 경로, userId: JWT
     * - 방기록 ID(roomRecordId), 랭킹(ranking)은 요청 바디에서 null 가능
     */
    @PostMapping("/{poseId}")
    @Operation(summary = "요가 포즈 기록 저장")
    public ResponseEntity<PoseRecordRes> createPoseRecord(
            @PathVariable Long poseId,
            @RequestBody PoseRecordRequest request
    ) {
        PoseRecord created = poseRecordService.createPoseRecord(poseId, request);
        return ResponseEntity.ok(PoseRecordRes.fromEntity(created));
    }

    /**
     * GET /api/yoga/history/{poseId}
     * - 특정 요가 포즈 기록 조회 (현재 사용자 기준)
     * - poseId: 경로, userId: JWT
     */
    @GetMapping("/{poseId}")
    @Operation(summary = "특정 요가 포즈 기록 조회")
    public ResponseEntity<List<PoseRecordRes>> getPoseRecordsByPoseId(@PathVariable Long poseId) {
        List<PoseRecord> records = poseRecordService.getPoseRecordsByPoseId(poseId);
        List<PoseRecordRes> response = records.stream()
                .map(PoseRecordRes::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/yoga/history
     * - 현재 사용자 기준, 모든 요가 포즈 기록 조회
     * - userId: JWT
     */
    @GetMapping
    @Operation(summary = "전체 요가 포즈 기록 조회")
    public ResponseEntity<List<PoseRecordRes>> getAllPoseRecords() {
        List<PoseRecord> records = poseRecordService.getAllPoseRecords();
        List<PoseRecordRes> response = records.stream()
                .map(PoseRecordRes::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
}
