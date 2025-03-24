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
    private Long poseId;
    private Long userOrderIndex;
    private Long createdAt;

    public static UserCoursePoseRes fromEntity(UserCoursePose entity) {
        return UserCoursePoseRes.builder()
                .userCoursePoseId(entity.getUserCoursePoseId())
                .poseId(entity.getPose().getPoseId())
                .userOrderIndex(entity.getUserOrderIndex())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
