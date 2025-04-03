package com.red.yogaback.websocket.service;

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

    // 라운드 관련 필드
    private int currentRound = 0;
    private int totalRounds = 0;
    private long roundStartTime = 0L;
    private long roundDuration = 0L; // 각 라운드 지속 시간 (밀리초)

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
    
    public void removeReadyUser(String userId) {
        readyUsers.remove(userId);
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

    // 라운드 관리 메서드
    public void startCourse(int totalRounds, long roundDuration) {
        this.totalRounds = totalRounds;
        this.currentRound = 1;
        this.roundDuration = roundDuration;
        this.roundStartTime = System.currentTimeMillis();
        this.courseStarted = true;
    }

    // 현재 라운드가 종료되었는지 체크 (예: 라운드 지속시간이 경과되었는지)
    public boolean isRoundEnded() {
        long now = System.currentTimeMillis();
        return (now - roundStartTime) >= roundDuration;
    }

    // 다음 라운드를 시작 (현재 라운드를 종료 후)
    public void nextRound() {
        if (hasNextRound()) {
            currentRound++;
            roundStartTime = System.currentTimeMillis();
        }
    }

    public boolean hasNextRound() {
        return currentRound < totalRounds;
    }

    public int getCurrentRound() {
        return currentRound;
    }
}
