package com.red.yogaback.model;

import lombok.Getter;

@Getter
public enum BadgeType {
    FIRST_EXERCISE(1L, "처음 운동", 1),
    CONSECUTIVE_DAYS(2L, "연속 운동", 3),  // 10일, 20일, 30일
    FIRST_MULTIPLAYER(3L, "첫 멀티플레이", 1),
    ROOM_WINS(4L, "방 우승", 3),  // 1회, 3회, 5회
    YOGA_COURSES(5L, "요가 코스", 3),  // 1개, 3개, 5개
    YOGA_ACCURACY(6L, "요가의 달인", 3),  // 70%, 80%, 90%
    YOGA_POSETIME(7L, "요가 유지", 1);  // 15초 이상

    private final Long id;
    private final String name;
    private final int maxLevel;

    BadgeType(Long id, String name,  int maxLevel) {
        this.id = id;
        this.name = name;

        this.maxLevel = maxLevel;
    }
}
