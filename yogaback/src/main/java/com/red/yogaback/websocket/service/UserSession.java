package com.red.yogaback.websocket.service;

public class UserSession {
    private String userId;
    private String roomId;
    private String userNickName;
    private String userProfile;

    public UserSession(String userId, String roomId, String userNickName, String userProfile) {
        this.userId = userId;
        this.roomId = roomId;
        this.userNickName = userNickName;
        this.userProfile = userProfile;
    }

    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
    public String getRoomId() {
        return roomId;
    }
    public void setRoomId(String roomId) {
        this.roomId = roomId;
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
}