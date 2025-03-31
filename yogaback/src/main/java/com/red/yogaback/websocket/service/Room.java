package com.red.yogaback.websocket.service;
//방 정보를 관리하며, 내부적으로 참가자와 준비한 사용자 목록을 저장합니다.
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class Room {
    private final String roomId;
    // sessionId -> userId 매핑
    private final Map<String, String> participants = new ConcurrentHashMap<>();
    // 준비 완료한 사용자 ID 집합
    private final Set<String> readyUsers = new CopyOnWriteArraySet<>();
    private boolean courseStarted = false;

    public Room(String roomId) {
        this.roomId = roomId;
    }

    public String getRoomId() {
        return roomId;
    }

    // 참가자 관리
    public void addParticipant(String sessionId, String userId) {
        participants.put(sessionId, userId);
    }

    public void removeParticipant(String sessionId, String userId) {
        participants.remove(sessionId);
        readyUsers.remove(userId);
    }

    public boolean hasParticipant(String sessionId) {
        return participants.containsKey(sessionId);
    }

    public int getParticipantCount() {
        return participants.size();
    }

    // 준비 상태 관리
    public void addReadyUser(String userId) {
        readyUsers.add(userId);
    }

    public int getReadyUserCount() {
        return readyUsers.size();
    }

    public boolean allUsersReady() {
        return !participants.isEmpty() && readyUsers.size() == participants.size();
    }

    public boolean isCourseStarted() {
        return courseStarted;
    }

    public void setCourseStarted(boolean courseStarted) {
        this.courseStarted = courseStarted;
    }
}
