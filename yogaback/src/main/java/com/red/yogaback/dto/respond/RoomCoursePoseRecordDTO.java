package com.red.yogaback.dto.respond;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RoomCoursePoseRecordDTO {
    private String userName;
    private String poseUrl;
    private Float poseTime;
    private Float accuracy;
    private Integer ranking;
}
