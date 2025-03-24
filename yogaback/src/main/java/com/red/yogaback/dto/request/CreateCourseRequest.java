package com.red.yogaback.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateCourseRequest {

    private String courseName; // "내 커스텀 코스"
    private List<PoseInfo> poses; // [{poseId: 1, userOrderIndex: 1}, ...]

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PoseInfo {
        private Long poseId;
        private Long userOrderIndex;
    }
}
