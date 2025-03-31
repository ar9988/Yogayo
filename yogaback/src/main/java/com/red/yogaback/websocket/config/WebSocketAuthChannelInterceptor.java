package com.red.yogaback.websocket.config;

import com.red.yogaback.security.jwt.JWTUtil;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    @Autowired
    private JWTUtil jwtUtil;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor != null) {
            // CONNECT 메시지일 때만 처리
            if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                List<String> tokenList = accessor.getNativeHeader("token");
                if (tokenList == null || tokenList.isEmpty()) {
                    throw new IllegalArgumentException("인증 토큰이 필요합니다.");
                }
                String token = tokenList.get(0);
                // 여러분의 JWTUtil을 사용하여 토큰 검증 및 클레임 추출
                // JWTUtil.java에서는 CustomException을 발생시키므로 try-catch로 감싸도 좋습니다.

                try {
                    // 아래와 같이 JWTUtil의 메서드를 호출하여 클레임을 추출하세요.
                    // 예시로 getMemberId()를 호출하기 전에 토큰이 유효한지 확인하는 로직을 추가할 수 있습니다.
                    if (jwtUtil.isExpired(token)) {
                        throw new IllegalArgumentException("토큰이 만료되었습니다.");
                    }
                    // 토큰의 클레임을 직접 추출하는 메서드가 없으면, getMemberId, getUserNickName, getUserProfile 메서드를 각각 호출할 수 있습니다.
                    Long memberId = jwtUtil.getMemberId(token);
                    String userNickName = jwtUtil.getUserNickName(token);
                    String userProfile = jwtUtil.getUserProfile(token);

                    // 만약 별도의 클레임 추출 메서드가 있다면 그것을 사용하는 것도 가능합니다.
                    // claims = jwtUtil.getClaimsFromToken(token);

                    accessor.getSessionAttributes().put("userId", memberId);
                    accessor.getSessionAttributes().put("userNickName", userNickName);
                    accessor.getSessionAttributes().put("userProfile", userProfile);
                } catch (Exception e) {
                    throw new IllegalArgumentException("유효하지 않은 토큰입니다.");
                }
            }
        }
        return message;
    }
}