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
    private Long roomRecordId;  // null 가능
    private Float accuracy;
    private Integer ranking;    // null 가능
    private Float poseTime;
    private String recordImg;
    private Long createdAt;

    /**
     * PoseRecord 엔티티 -> PoseRecordRes 변환
     */
    public static PoseRecordRes fromEntity(PoseRecord entity) {
        return PoseRecordRes.builder()
                .poseRecordId(entity.getPoseRecordId())
                .poseId(entity.getPose().getPoseId())
                .roomRecordId(entity.getRoomRecord() == null ? null : entity.getRoomRecord().getRoomRecordId())
                .accuracy(entity.getAccuracy())
                .ranking(entity.getRanking())
                .poseTime(entity.getPoseTime())
                .recordImg(entity.getRecordImg())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
