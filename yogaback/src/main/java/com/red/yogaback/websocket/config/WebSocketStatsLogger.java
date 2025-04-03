package com.red.yogaback.websocket.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.config.WebSocketMessageBrokerStats;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class WebSocketStatsLogger {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketStatsLogger.class);

    @Autowired
    private WebSocketMessageBrokerStats brokerStats;

    @Scheduled(fixedDelay = 10000) // 10초마다 실행
    public void logStats() {
        String stats = brokerStats.toString();
        logger.info("WebSocket Broker Stats: {}", stats);

    }
} 