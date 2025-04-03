package com.red.yogaback.websocket.dto;

import java.util.Objects;

public class UserSignalDTO {
    private final String userId;
    private final String userNickName;
    private final String userProfile;
    private final String signal;

    public UserSignalDTO(String userId, String userNickName, String userProfile, String signal) {
        this.userId = Objects.requireNonNull(userId, "userId must not be null");
        this.userNickName = Objects.requireNonNull(userNickName, "userNickName must not be null");
        this.userProfile = Objects.requireNonNull(userProfile, "userProfile must not be null");
        this.signal = Objects.requireNonNull(signal, "signal must not be null");
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
