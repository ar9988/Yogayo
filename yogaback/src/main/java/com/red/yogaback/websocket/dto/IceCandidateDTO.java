package com.red.yogaback.websocket.dto;

public class IceCandidateDTO {
    private String userId;
    private String userNickName;
    private String userProfile;
    private String candidate;

    public IceCandidateDTO(String userId, String userNickName, String userProfile, String candidate) {
        this.userId = userId;
        this.userNickName = userNickName;
        this.userProfile = userProfile;
        this.candidate = candidate;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserNickName() {
        return userNickName;
    }

    public String getUserProfile() {
        return userProfile;
    }

    public String getCandidate() {
        return candidate;
    }
}
