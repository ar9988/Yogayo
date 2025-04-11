package com.red.yogaback.dto.respond;

import com.red.yogaback.model.UserCourse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCourseRes {
    private Long userCourseId;
    private String courseName;
    private boolean tutorial;
    private Long createdAt;
    private Long modifyAt;

    // ★ userCoursePoseRes 대신, pose 정보만 담을 List<PoseRes>
    private List<PoseRes> poses;

    public static UserCourseRes fromEntity(UserCourse entity) {
        return UserCourseRes.builder()
                .userCourseId(entity.getUserCourseId())
                .courseName(entity.getCourseName())
                .tutorial(entity.isTutorial())
                .createdAt(entity.getCreatedAt())
                .modifyAt(entity.getModifyAt())

                // userCoursePose 목록 -> userOrderIndex 순으로 정렬 -> PoseRes 변환
                .poses(
                        entity.getUserCoursePoses() == null
                                ? null
                                : entity.getUserCoursePoses().stream()
                                // userOrderIndex 순서대로 정렬
                                .sorted((a, b) -> {
                                    Long idxA = a.getUserOrderIndex() != null ? a.getUserOrderIndex() : 0L;
                                    Long idxB = b.getUserOrderIndex() != null ? b.getUserOrderIndex() : 0L;
                                    return idxA.compareTo(idxB);
                                })
                                // PoseRes로 변환
                                .map(ucp -> PoseRes.fromEntity(ucp.getPose()))
                                .collect(Collectors.toList())
                )
                .build();
    }
}
