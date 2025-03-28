package com.red.yogaback.dto.respond;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * GET /api/yoga/history 응답용 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PoseHistorySummaryRes {
    private Long poseId;
    private String poseName;
    private String poseImg;
    private Float bestAccuracy; // 기록이 없으면 0 또는 null 처리
    private Float bestTime;     // 기록이 없으면 0 또는 null 처리
}
