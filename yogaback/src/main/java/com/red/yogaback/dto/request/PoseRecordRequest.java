package com.red.yogaback.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 요가 기록 생성/수정 시 JSON으로 전달되는 요청 데이터
 * ranking은 선택사항입니다.
 * (기존 roomRecordId 필드는 제거되었습니다.)
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PoseRecordRequest {
    private Float accuracy;
    private Integer ranking;    // 선택사항
    private Float poseTime;
    // recordImg는 MultipartFile로 별도 받으므로 JSON에는 포함하지 않음
}
