package com.red.yogaback.controller;

import com.red.yogaback.dto.request.PoseRecordRequest;
import com.red.yogaback.dto.respond.PoseDetailHistoryRes;
import com.red.yogaback.dto.respond.PoseHistorySummaryRes;
import com.red.yogaback.dto.respond.PoseRecordRes;
import com.red.yogaback.model.PoseRecord;
import com.red.yogaback.service.PoseRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@Tag(name = "요가 기록 API", description = "PoseRecord를 이용한 요가 포즈 기록 관리")
@RequestMapping("/api/yoga/history")
@RequiredArgsConstructor
public class PoseRecordController {

    private final PoseRecordService poseRecordService;

    /**
     * [POST] /api/yoga/history/{poseId}
     * - 요가 포즈 기록 저장
     */
    @PostMapping(value = "/{poseId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "요가 포즈 기록 저장")
    public ResponseEntity<PoseRecordRes> createPoseRecord(
            @PathVariable Long poseId,
            @RequestPart("poseRecordRequest") PoseRecordRequest request,
            @RequestPart(value = "recordImg", required = false) MultipartFile recordImg
    ) {
        PoseRecord created = poseRecordService.createPoseRecord(poseId, request, recordImg);
        return ResponseEntity.ok(PoseRecordRes.fromEntity(created));
    }

    /**
     * [GET] /api/yoga/history
     * - 전체 요가 포즈 기록 조회
     * - 각 포즈별로 bestAccuracy, bestTime을 구해 리스트로 반환
     */
    @GetMapping
    @Operation(summary = "전체 요가 포즈 기록 조회")
    public ResponseEntity<List<PoseHistorySummaryRes>> getAllPoseRecords() {
        List<PoseHistorySummaryRes> response = poseRecordService.getAllPoseRecordsSummary();
        return ResponseEntity.ok(response);
    }

    /**
     * [GET] /api/yoga/history/{poseId}
     * - 특정 요가 포즈 기록 조회
     * - bestAccuracy, bestTime, winCount(랭킹 1인 횟수), histories 배열
     */
    @GetMapping("/{poseId}")
    @Operation(summary = "특정 요가 포즈 기록 조회")
    public ResponseEntity<PoseDetailHistoryRes> getPoseDetailHistory(@PathVariable Long poseId) {
        PoseDetailHistoryRes response = poseRecordService.getPoseDetailHistory(poseId);
        return ResponseEntity.ok(response);
    }
}
