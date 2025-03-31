package com.red.yogaback.websocket.service;
//사용자 세션에 대한 정보를 담는 단순 데이터 클래스입니다.
//필드: userId, roomId, userNickName, userProfile
public final class UserSession {
    private final String userId;
    private final String roomId;
    private final String userNickName;
    private final String userProfile;

    public UserSession(String userId, String roomId, String userNickName, String userProfile) {
        this.userId = userId;
        this.roomId = roomId;
        this.userNickName = userNickName;
        this.userProfile = userProfile;
    }

    public String getUserId() {
        return userId;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getUserNickName() {
        return userNickName;
    }

    public String getUserProfile() {
        return userProfile;
    }
}
