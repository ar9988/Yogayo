package com.red.yogaback.controller;

import com.red.yogaback.dto.respond.PoseListRes;
import com.red.yogaback.service.PoseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "요가 포즈 관련 API", description = "요가 포즈 조회 기능")
@RequestMapping("/api/yoga")
@RequiredArgsConstructor
public class PoseController {

    private final PoseService poseService;

    @Operation(summary = "모든 요가 포즈 조회", description = "시스템에 등록된 모든 요가 포즈 정보를 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "요가 포즈 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = PoseListRes.class))),
            @ApiResponse(responseCode = "404", description = "요가 포즈를 찾을 수 없음")
    })
    @GetMapping("/all")
    public ResponseEntity<List<PoseListRes>> getAllPoses() {
        List<PoseListRes> poses = poseService.getAllPoses();

        if (poses.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(poses);
    }
}
