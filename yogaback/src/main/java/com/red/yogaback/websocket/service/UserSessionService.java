package com.red.yogaback.websocket.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserSessionService {
    // sessionId -> UserSession
    private Map<String, UserSession> sessions = new ConcurrentHashMap<>();

    public void addSession(String sessionId, UserSession userSession) {
        sessions.put(sessionId, userSession);
    }

    public UserSession getSession(String sessionId) {
        return sessions.get(sessionId);
    }

    public void removeSession(String sessionId) {
        sessions.remove(sessionId);
    }
}