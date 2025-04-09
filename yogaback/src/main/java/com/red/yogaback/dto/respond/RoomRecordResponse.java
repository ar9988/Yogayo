package com.red.yogaback.dto.respond;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 저장된 RoomRecord의 결과를 반환할 때 사용하는 응답 DTO.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomRecordResponse {
    private Long roomRecordId;
    private Long userId;
    private Long roomId;
    private Integer totalRanking;
    private Integer totalScore;
    private Long createdAt;
}
