package com.red.yogaback.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 요가 기록 생성/수정 시 JSON으로 전달되는 요청 데이터
 * roomRecordId, ranking은 null 가능
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PoseRecordRequest {
    private Long roomRecordId;  // 선택사항 (멀티모드 기록 시)
    private Float accuracy;
    private Integer ranking;    // 선택사항
    private Float poseTime;
    // recordImg는 MultipartFile로 별도 받으므로 JSON에는 포함하지 않음
}
