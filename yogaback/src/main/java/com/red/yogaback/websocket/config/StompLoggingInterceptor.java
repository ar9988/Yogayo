package com.red.yogaback.websocket.config;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class StompLoggingInterceptor implements ChannelInterceptor {
    // SLF4J 로거 초기화
    private static final Logger logger = LoggerFactory.getLogger(StompLoggingInterceptor.class);

    /**
     * 메시지가 전송되기 전에 STOMP 메시지 타입, 페이로드, 헤더 정보를 로깅합니다.
     * 개선방향:
     *  - DEBUG 레벨 로그의 경우, logger.isDebugEnabled()를 활용하여 조건부 로깅을 구현하면 불필요한 연산을 줄일 수 있음.
     *  - 민감한 정보가 있다면 로깅 시 보안 고려 (현재 코드에서는 일반 정보만 로깅됨).
     */
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        // 메시지 헤더에서 "simpMessageType" 값을 추출하여 로깅
        Object command = message.getHeaders().get("simpMessageType");
        logger.info("STOMP Message Type: {}", command);

        // 메시지 페이로드가 null이 아닌 경우 로깅
        if (message.getPayload() != null) {
            logger.info("Message Payload: {}", message.getPayload());
        }

        // 개선방향:
        // - 헤더 정보를 로깅할 때, logger.isDebugEnabled() 체크를 추가하여 DEBUG 레벨일 경우에만 로깅하도록 함.
        // 헤더 정보 로깅 (모든 헤더 key와 value 출력)
        message.getHeaders().forEach((key, value) -> {
            logger.debug("Header - {}: {}", key, value);
        });

        // 메시지를 변경하지 않고 그대로 반환 (후속 처리 진행)
        return message;
    }
}
