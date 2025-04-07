//클라이언트의 STOMP CONNECT 메시지를 가로채 JWT 토큰의 유효성을 검사하고,
// 유효한 토큰의 경우 사용자 정보를 세션에 저장합니다.
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

    // SLF4J 로거 초기화
    private static final Logger logger = LoggerFactory.getLogger(WebSocketAuthChannelInterceptor.class);

    private final JWTUtil jwtUtil;

    // JWTUtil을 의존성 주입받음
    @Autowired
    public WebSocketAuthChannelInterceptor(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * 클라이언트의 STOMP CONNECT 메시지를 가로채 JWT 토큰의 유효성을 검사하고,
     * 토큰에서 추출한 사용자 정보를 세션 속성에 저장하는 메소드입니다.
     *
     * @param message  클라이언트가 보낸 메시지
     * @param channel  메시지가 전송될 채널
     * @return         원본 메시지 (변경 없이 반환)
     * @throws IllegalArgumentException 토큰이 없거나 유효하지 않을 경우 발생
     */
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        // STOMP 메시지 헤더에 접근하기 위해 StompHeaderAccessor를 사용합니다.
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        // CONNECT 명령에 대해서만 JWT 토큰 인증 로직을 수행합니다.
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            // "Authorization" 헤더를 리스트 형태로 추출
            List<String> authHeaderList = accessor.getNativeHeader("Authorization");
            String token = null;

            // 헤더 리스트가 null이 아니며, 값이 존재하면 토큰 추출 시도
            if (authHeaderList != null && !authHeaderList.isEmpty()) {
                String authHeader = authHeaderList.get(0);
                // Improvement: 토큰 추출 로직을 별도의 메소드로 분리하면 가독성이 좋아질 수 있음.
                // "Bearer " 접두사가 있는 경우 접두사를 제거하여 실제 토큰만 추출합니다.
                if (authHeader != null && authHeader.toLowerCase().startsWith("bearer ")) {
                    token = authHeader.substring(7); // "Bearer " 이후 부분이 실제 토큰
                } else {
                    // Improvement: 접두사 체크 없이 바로 토큰을 사용하면 예상치 못한 문제가 발생할 수 있음.
                    token = authHeader;
                }
            }

            // 토큰이 없거나 빈 문자열일 경우 경고 로그를 남기고 예외 발생
            if (token == null || token.trim().isEmpty()) {
                logger.warn("No valid token found in Authorization header");
                throw new IllegalArgumentException("인증 토큰이 필요합니다.");
            }

            try {
                // 토큰이 만료되었는지 확인
                if (jwtUtil.isExpired(token)) {
                    logger.warn("Expired token"); // Improvement: 보안상 토큰 값 자체를 로그에 남기지 않는 것이 좋음.
                    throw new IllegalArgumentException("토큰이 만료되었습니다.");
                }
                // 토큰에서 사용자 정보를 추출
                Long memberId = jwtUtil.getMemberId(token);
                String userNickName = jwtUtil.getUserNickName(token);
                String userProfile = jwtUtil.getUserProfile(token);

                // 추출한 정보를 세션 속성에 저장하여 이후에 사용
                accessor.getSessionAttributes().put("userId", String.valueOf(memberId));
                accessor.getSessionAttributes().put("userNickName", userNickName);
                accessor.getSessionAttributes().put("userProfile", userProfile);
                logger.info("Token validated for user: {}", memberId);
            } catch (Exception e) {
                // Improvement: 구체적인 인증 예외 처리를 위해 커스텀 예외 사용 고려
                logger.error("Token validation error: {}", e.getMessage());
                throw new IllegalArgumentException("유효하지 않은 토큰입니다.");
            }
        }
        // 메시지를 그대로 반환하여 후속 처리에 전달
        return message;
    }
}
