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
    private int roomCount;
    private String userNickname;
    private String roomName;
    private String password;

    @JsonProperty("hasPassword")
    private boolean hasPassword;
    List<PoseDetail> pose;

    @Getter
    @Builder
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PoseDetail {
        private Long poseId;
        private String poseName;
        private String poseDescription;
        private String poseImg;
        private long poseLevel;
        private String poseVideo;
        private long setPoseId;
        private int userOrderIndex;

    }





}
