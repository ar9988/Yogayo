package com.red.yogaback.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
@JsonPropertyOrder({ "roomId", "roomMax", "userNickname", "roomName", "hasPassword", "password", "userCourse" })
public class RoomRequest {
    private Long roomId;
    private int roomMax;
    private String userNickname;
    private String roomName;
    private String password;

    @JsonProperty("hasPassword")
    private boolean hasPassword;

    private UserCourseRequest userCourse;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserCourseRequest {
        private Long courseId;
        private String courseName;
        private boolean tutorial;
        private List<PoseDetail> poses;

        @Getter
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class PoseDetail {
            private Long poseId;
            private String poseName;
            private String poseDescription;
            private String poseImg;
            private int poseLevel;
            private String poseVideo;
            private int setPoseId;
        }
    }
}
