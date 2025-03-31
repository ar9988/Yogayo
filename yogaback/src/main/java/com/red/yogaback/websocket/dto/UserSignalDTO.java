package com.red.yogaback.websocket.dto;

public class UserSignalDTO {
    private String userId;
    private String userNickName;
    private String userProfile;
    private String signal;

    public UserSignalDTO(String userId, String userNickName, String userProfile, String signal) {
        this.userId = userId;
        this.userNickName = userNickName;
        this.userProfile = userProfile;
        this.signal = signal;
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

    public String getSignal() {
        return signal;
    }
}
