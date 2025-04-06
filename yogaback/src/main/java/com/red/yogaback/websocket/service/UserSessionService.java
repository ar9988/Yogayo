package com.red.yogaback.websocket.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserSessionService {
    // SLF4J 로거 초기화
    private static final Logger logger = LoggerFactory.getLogger(UserSessionService.class);

    // sessionId -> UserSession 매핑 (동시성 보장 위해 ConcurrentHashMap 사용)
    private final Map<String, UserSession> sessions = new ConcurrentHashMap<>();

    /**
     * 새로운 세션을 등록합니다.
     *
     * @param sessionId    WebSocket 세션 ID
     * @param userSession  해당 세션의 사용자 정보
     *
     * 개선방향:
     *  - 동일한 세션 ID가 이미 존재할 때 덮어쓰기보다는 경고 로그를 남기거나 예외 처리할 수 있습니다.
     *  - 세션 수명 주기(만료 시간) 관리가 필요하다면, 타임아웃 스케줄러나 만료 검사 로직을 추가할 수 있습니다.
     */
    public void addSession(String sessionId, UserSession userSession) {
        sessions.put(sessionId, userSession);
        logger.info("Session added: {} for user {}", sessionId, userSession.getUserId());
    }

    /**
     * 세션 ID로 UserSession을 조회합니다.
     *
     * @param sessionId WebSocket 세션 ID
     * @return 등록된 UserSession, 없으면 null
     *
     * 개선방향:
     *  - Optional<UserSession> 반환으로 null 체크를 강제할 수 있습니다.
     *  - 캐시 미스 발생 시 로깅 또는 모니터링 이벤트를 발생시킬 수 있습니다.
     */
    public UserSession getSession(String sessionId) {
        return sessions.get(sessionId);
    }

    /**
     * 세션을 제거합니다.
     *
     * @param sessionId WebSocket 세션 ID
     *
     * 개선방향:
     *  - 제거 전 존재 여부를 체크하여, 존재하지 않을 경우 경고 로그를 남길 수 있습니다.
     *  - 제거 이벤트를 발행하여 다른 컴포넌트가 후속 처리를 할 수 있도록 할 수 있습니다.
     */
    public void removeSession(String sessionId) {
        sessions.remove(sessionId);
        logger.info("Session removed: {}", sessionId);
    }

    /*
     * 추가 개선방향:
     * 1. 동시성 사용 통계 수집:
     *    - 현재 활성 세션 수, 추가/제거 빈도 등을 모니터링하여 시스템 상태를 파악할 수 있습니다.
     * 2. 세션 정보 조회 API:
     *    - 특정 사용자 ID로 세션 조회, 모든 활성 세션 목록 반환 등의 기능을 추가할 수 있습니다.
     * 3. 리소스 정리:
     *    - 세션 제거 시 연결 서비스나 방 서비스 등 다른 컴포넌트와 연계해 자원 해제 로직을 트리거할 수 있습니다.
     */
}
