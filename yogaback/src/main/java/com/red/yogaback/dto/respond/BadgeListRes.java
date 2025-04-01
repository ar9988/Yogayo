package com.red.yogaback.dto.respond;

import lombok.AllArgsConstructor;
import lombok.Getter;


import java.util.List;

@Getter
@AllArgsConstructor
public class BadgeListRes {
    private Long badgeId;
    private String badgeName;
    private int badgeProgress;
    private int highLevel;
    private List<BadgeDetailRes> badgeDetails;

    @Getter
    @AllArgsConstructor
    public static class BadgeDetailRes {
        private Long badgeDetailId;
        private String badgeDetailName;
        private String badgeDetailImg;
        private String badgeDescription;
        private int badgeGoal;
        private int badgeLevel;
    }

}
