package com.red.yogaback.websocket.dto;

public class ReadyMessage {
    private int userId;
    private String userNickName;
    private String userProfile;
    private boolean isReady;

    public ReadyMessage() {
    }

    public ReadyMessage(int userId, String userNickName, String userProfile, boolean isReady) {
        this.userId = userId;
        this.userNickName = userNickName;
        this.userProfile = userProfile;
        this.isReady = isReady;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserNickName() {
        return userNickName;
    }

    public void setUserNickName(String userNickName) {
        this.userNickName = userNickName;
    }

    public String getUserProfile() {
        return userProfile;
    }

    public void setUserProfile(String userProfile) {
        this.userProfile = userProfile;
    }

    public boolean isReady() {
        return isReady;
    }

    public void setReady(boolean ready) {
        isReady = ready;
    }
}
