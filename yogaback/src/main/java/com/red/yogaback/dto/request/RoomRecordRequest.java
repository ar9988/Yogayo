package com.red.yogaback.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 클라이언트가 최종 기록을 저장할 때 전달하는 데이터.
 * 방의 최종 기록(최종 등수 및 최종 점수)을 담습니다.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RoomRecordRequest {
    private Long roomId;
    private Integer totalRanking;
    private Integer totalScore;
}
