package com.red.yogaback.dto.request;

import lombok.Data;

@Data
public class ScoreMessage {
    private String roomId;
    private String userId;
    private int score;
    private double duration; // 예: 자세 유지 시간
    private int roundIndex; // 라운드 번호 (필요한 경우)
}