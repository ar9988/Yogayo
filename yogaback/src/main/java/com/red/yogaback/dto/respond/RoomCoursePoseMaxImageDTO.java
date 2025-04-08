package com.red.yogaback.dto.respond;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RoomCoursePoseMaxImageDTO {
    private String poseName;
    private String poseUrl;
    private int roomOrderIndex;
}
