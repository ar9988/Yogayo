package com.red.yogaback.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 요가 기록 생성/수정 시 요청 바디
 * - roomRecordId, ranking은 null 가능
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PoseRecordRequest {
    private Long roomRecordId;  // null 가능
    private Float accuracy;
    private Integer ranking;    // null 가능
    private Float poseTime;
    private String recordImg;
}
