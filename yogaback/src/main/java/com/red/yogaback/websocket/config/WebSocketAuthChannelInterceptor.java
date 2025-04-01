package com.red.yogaback.websocket.config;
//클라이언트가 CONNECT 시 JWT 토큰을 헤더에서 추출하여 유효성 검사하고, 클레임(회원번호, 닉네임, 프로필 등)을 세션 속성에 저장합니다.
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

    @Autowired
    private JWTUtil jwtUtil;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            List<String> tokenList = accessor.getNativeHeader("token");
            if (tokenList == null || tokenList.isEmpty()) {
                logger.warn("No token provided in CONNECT message");
                throw new IllegalArgumentException("인증 토큰이 필요합니다.");
            }
            String token = tokenList.get(0);

            try {
                if (jwtUtil.isExpired(token)) {
                    logger.warn("Expired token for token: {}", token);
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
                throw new IllegalArgumentException("유효하지 않은 토큰입니다.");
            }
        }
        return message;
    }
}
