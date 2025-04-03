package com.red.yogaback.websocket.config;

import com.red.yogaback.security.jwt.JWTUtil;
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

    @Autowired
    public WebSocketAuthChannelInterceptor(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            // "Authorization" 헤더를 찾습니다.
            List<String> authHeaderList = accessor.getNativeHeader("Authorization");
            String token = null;

            if (authHeaderList != null && !authHeaderList.isEmpty()) {
                String authHeader = authHeaderList.get(0);
                // "Bearer " 접두사가 있는지 확인하고 제거합니다.
                if (authHeader != null && authHeader.toLowerCase().startsWith("bearer ")) {
                    token = authHeader.substring(7); // "Bearer " 다음부터 실제 토큰
                } else {
                    // Bearer 타입이 아니거나 형식이 잘못된 경우 처리 (선택적)
                    logger.warn("Invalid Authorization header format.");
                    // token = authHeader; // 만약 Bearer 없이 토큰만 올 수도 있다면
                }
            }

            if (token == null || token.trim().isEmpty()) { // 추출된 토큰이 있는지 확인
                logger.warn("No valid token found in Authorization header");
                throw new IllegalArgumentException("인증 토큰이 필요합니다.");
            }

            // --- 이하 토큰 유효성 검사 로직은 동일 ---
            try {
                if (jwtUtil.isExpired(token)) {
                    logger.warn("Expired token"); // 토큰 값 로그는 보안상 제거하는 것이 좋음
                    throw new IllegalArgumentException("토큰이 만료되었습니다.");
                }
                Long memberId = jwtUtil.getMemberId(token);
                String userNickName = jwtUtil.getUserNickName(token);
                String userProfile = jwtUtil.getUserProfile(token);

                accessor.getSessionAttributes().put("userId", String.valueOf(memberId));
                accessor.getSessionAttributes().put("userNickName", userNickName);
                accessor.getSessionAttributes().put("userProfile", userProfile);
                logger.info("Token validated for user: {}", memberId);
            } catch (Exception e) {
                logger.error("Token validation error: {}", e.getMessage());
                throw new IllegalArgumentException("유효하지 않은 토큰입니다."); // 구체적인 오류 대신 일반적인 메시지
            }
        }
        return message;
    }
}
