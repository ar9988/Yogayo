package com.red.yogaback.websocket.service;
//ConcurrentHashMap을 사용해 sessionId와 UserSession 간의 매핑을 관리합니다.
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserSessionService {
    private static final Logger logger = LoggerFactory.getLogger(UserSessionService.class);

    // sessionId -> UserSession
    private final Map<String, UserSession> sessions = new ConcurrentHashMap<>();

    public void addSession(String sessionId, UserSession userSession) {
        sessions.put(sessionId, userSession);
        logger.info("Session added: {} for user {}", sessionId, userSession.getUserId());
    }

    public UserSession getSession(String sessionId) {
        return sessions.get(sessionId);
    }

    public void removeSession(String sessionId) {
        sessions.remove(sessionId);
        logger.info("Session removed: {}", sessionId);
    }
}
