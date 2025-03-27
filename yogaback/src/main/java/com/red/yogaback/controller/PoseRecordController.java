package com.red.yogaback.controller;

import com.red.yogaback.dto.request.PoseRecordRequest;
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
import java.util.stream.Collectors;

@RestController
@Tag(name = "요가 기록 API", description = "PoseRecord를 이용한 요가 포즈 기록 관리")
@RequestMapping("/api/yoga/history")
@RequiredArgsConstructor
public class PoseRecordController {

    private final PoseRecordService poseRecordService;

    /**
     * POST /api/yoga/history/{poseId}
     * - 요가 포즈 기록 저장
     * - poseId는 경로, userId는 JWT에서 추출
     * - 요청 본문은 멀티파트 형식으로, JSON 부분은 "poseRecordRequest",
     *   이미지 파일은 "recordImg"로 전달 (파일은 선택사항)
     */
    @PostMapping(value = "/{poseId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "요가 포즈 기록 저장")
    public ResponseEntity<PoseRecordRes> createPoseRecord(
            @PathVariable Long poseId,
            @RequestPart("poseRecordRequest") PoseRecordRequest request,
            @RequestPart(value = "recordImg", required = false) MultipartFile recordImg) {
        PoseRecord created = poseRecordService.createPoseRecord(poseId, request, recordImg);
        return ResponseEntity.ok(PoseRecordRes.fromEntity(created));
    }

    /**
     * GET /api/yoga/history/{poseId}
     * - 특정 요가 포즈 기록 조회 (현재 사용자 기준)
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
