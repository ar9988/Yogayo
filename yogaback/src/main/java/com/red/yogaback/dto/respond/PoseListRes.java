package com.red.yogaback.dto.respond;

import com.red.yogaback.model.Pose;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PoseListRes {
    private Long poseId;
    private String poseName;
    private String poseDescription;
    private String poseImg;
    private Long poseLevel;
    private String poseVideo;
    private Long setPoseId;
    private String poseAnimation;

    // Pose 엔티티를 PoseListRes DTO로 변환하는 정적 메서드
    public static PoseListRes fromEntity(Pose pose) {
        return PoseListRes.builder()
                .poseId(pose.getPoseId())
                .poseName(pose.getPoseName())
                .poseDescription(pose.getPoseDescription())
                .poseImg(pose.getPoseImg())
                .poseLevel(pose.getPoseLevel())
                .poseVideo(pose.getPoseVideo())
                .setPoseId(pose.getSetPose() != null ? pose.getSetPose().getPoseId() : null)
                .poseAnimation(pose.getPoseAnimation())
                .build();
    }
}
