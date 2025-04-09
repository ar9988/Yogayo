package com.red.yogaback.dto.respond;

import com.red.yogaback.model.PoseRecord;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 요가 기록 조회 시 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PoseRecordRes {
    private Long poseRecordId;
    private Long poseId;
    private Long roomId;  // 변경: 기존 roomRecordId 대신 room의 roomId를 반환
    private Float accuracy;
    private Integer ranking;    // null 가능
    private Float poseTime;
    private String recordImg;
    private Long createdAt;

    public static PoseRecordRes fromEntity(PoseRecord entity) {
        return PoseRecordRes.builder()
                .poseRecordId(entity.getPoseRecordId())
                .poseId(entity.getPose().getPoseId())
                .roomId(entity.getRoom() == null ? null : entity.getRoom().getRoomId())
                .accuracy(entity.getAccuracy())
                .ranking(entity.getRanking())
                .poseTime(entity.getPoseTime())
                .recordImg(entity.getRecordImg())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
