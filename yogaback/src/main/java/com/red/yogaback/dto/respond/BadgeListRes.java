package com.red.yogaback.dto.respond;

import lombok.Getter;

@Getter
public class BadgeListRes {
    private Long badgeId;
    private String badgeName;
    private String badgeImg;
    private String badgeCondition;
    private boolean isAchieved;

    public BadgeListRes(Long badgeId, String badgeName, String badgeImg, String badgeCondition, boolean isAchieved) {
        this.badgeId = badgeId;
        this.badgeName = badgeName;
        this.badgeImg = badgeImg;
        this.badgeCondition = badgeCondition;
        this.isAchieved = isAchieved;
    }
}
