package com.red.yogaback.dto.respond;

import com.red.yogaback.model.UserCoursePose;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCoursePoseRes {
    private Long userCoursePoseId;
    private PoseRes pose;            // ← 여기서 poseId 대신 PoseRes 전체를 가짐
    private Long userOrderIndex;
    private Long createdAt;

    public static UserCoursePoseRes fromEntity(UserCoursePose entity) {
        return UserCoursePoseRes.builder()
                .userCoursePoseId(entity.getUserCoursePoseId())
                // PoseRes 전체를 반환 -> poseName, poseDescription 등 모든 필드 포함
                .pose(PoseRes.fromEntity(entity.getPose()))
                .userOrderIndex(entity.getUserOrderIndex())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
