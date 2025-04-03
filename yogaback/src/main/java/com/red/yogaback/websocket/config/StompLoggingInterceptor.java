package com.red.yogaback.websocket.config;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class StompLoggingInterceptor implements ChannelInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(StompLoggingInterceptor.class);

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        Object command = message.getHeaders().get("simpMessageType");
        logger.info("STOMP Message Type: {}", command);
        
        // 메시지 본문 로깅
        if (message.getPayload() != null) {
            logger.info("Message Payload: {}", message.getPayload());
        }
        
        // 헤더 정보 로깅
        message.getHeaders().forEach((key, value) -> {
            logger.debug("Header - {}: {}", key, value);
        });
        
        return message;
    }
} 