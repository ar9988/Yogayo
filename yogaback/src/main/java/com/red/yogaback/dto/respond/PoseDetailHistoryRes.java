package com.red.yogaback.dto.respond;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * GET /api/yoga/history/{poseId} 응답용 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PoseDetailHistoryRes {
    private Long poseId;
    private String poseName;
    private String poseImg;
    private Float bestAccuracy;
    private Float bestTime;
    private int winCount;  // ranking == 1인 기록 수 (없으면 0)
    private List<HistoryItem> histories;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HistoryItem {
        private Long historyId;
        private Long userId;
        private Float accuracy;
        private Integer ranking;  // null 가능
        private Float poseTime;
        private String recordImg; // null 가능
        private Long createdAt;   // 추가된 필드
    }
}
