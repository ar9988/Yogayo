package com.red.yogaback.websocket.config;

import com.red.yogaback.security.jwt.JWTUtil;
import com.red.yogaback.websocket.service.UserSession;
import com.red.yogaback.websocket.service.UserSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Component
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketAuthChannelInterceptor.class);

    private final JWTUtil jwtUtil;
    private final UserSessionService userSessionService;

    @Autowired
    public WebSocketAuthChannelInterceptor(JWTUtil jwtUtil, UserSessionService userSessionService) {
        this.jwtUtil = jwtUtil;
        this.userSessionService = userSessionService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        // STOMP 헤더 접근
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            List<String> authHeaderList = accessor.getNativeHeader("Authorization");
            String token = null;
            if (authHeaderList != null && !authHeaderList.isEmpty()) {
                String authHeader = authHeaderList.get(0);
                if (authHeader != null && authHeader.toLowerCase().startsWith("bearer ")) {
                    token = authHeader.substring(7); // "Bearer " 이후의 실제 토큰
                } else {
                    token = authHeader;
                }
            }
            if (token == null || token.trim().isEmpty()) {
                logger.warn("No valid token found in Authorization header");
                throw new IllegalArgumentException("인증 토큰이 필요합니다.");
            }
            try {
                if (jwtUtil.isExpired(token)) {
                    logger.warn("Expired token");
                    throw new IllegalArgumentException("토큰이 만료되었습니다.");
                }
                Long memberId = jwtUtil.getMemberId(token);
                String userNickName = jwtUtil.getUserNickName(token);
                String userProfile = jwtUtil.getUserProfile(token);

                // 세션 속성에 사용자 정보 저장
                accessor.getSessionAttributes().put("userId", String.valueOf(memberId));
                accessor.getSessionAttributes().put("userNickName", userNickName);
                accessor.getSessionAttributes().put("userProfile", userProfile);

                // 세션 등록 (아직 roomId 정보는 없으므로 빈 문자열)
                String sessionId = accessor.getSessionId();
                UserSession userSession = new UserSession(String.valueOf(memberId), "", userNickName, userProfile);
                userSessionService.addSession(sessionId, userSession);

                logger.info("Token validated and session registered for user: {}", memberId);
            } catch (Exception e) {
                logger.error("Token validation error: {}", e.getMessage());
                throw new IllegalArgumentException("유효하지 않은 토큰입니다.");
            }
        }
        return message;
    }
}
