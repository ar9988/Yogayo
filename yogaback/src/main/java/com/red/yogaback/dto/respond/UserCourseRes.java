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
    private List<UserCoursePoseRes> poses;

    public static UserCourseRes fromEntity(UserCourse entity) {
        return UserCourseRes.builder()
                .userCourseId(entity.getUserCourseId())
                .courseName(entity.getCourseName())
                .tutorial(entity.isTutorial())
                .createdAt(entity.getCreatedAt())
                .modifyAt(entity.getModifyAt())
                .poses(
                        entity.getUserCoursePoses().stream()
                                .map(UserCoursePoseRes::fromEntity)
                                .collect(Collectors.toList())
                )
                .build();
    }
}
