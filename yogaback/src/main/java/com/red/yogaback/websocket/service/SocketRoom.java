package com.red.yogaback.websocket.service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class SocketRoom {
    private final String roomId;
    // sessionId -> userId 매핑
    private final Map<String, String> participants = new ConcurrentHashMap<>();
    // 준비 완료한 사용자 ID 집합
    private final Set<String> readyUsers = new CopyOnWriteArraySet<>();
    private boolean courseStarted = false;

    public SocketRoom(String roomId) {
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
}
