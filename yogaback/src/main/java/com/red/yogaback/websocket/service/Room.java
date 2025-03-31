package com.red.yogaback.websocket.service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class Room {
    private String roomId;
    // sessionId -> userId 매핑
    private Map<String, String> participants = new ConcurrentHashMap<>();
    // 준비 완료한 사용자 ID 집합
    private Set<String> readyUsers = new CopyOnWriteArraySet<>();
    private boolean courseStarted = false;

    public Room(String roomId) {
        this.roomId = roomId;
    }

    public String getRoomId() {
        return roomId;
    }

    public Map<String, String> getParticipants() {
        return participants;
    }

    public Set<String> getReadyUsers() {
        return readyUsers;
    }

    public boolean isCourseStarted() {
        return courseStarted;
    }

    public void setCourseStarted(boolean courseStarted) {
        this.courseStarted = courseStarted;
    }
}