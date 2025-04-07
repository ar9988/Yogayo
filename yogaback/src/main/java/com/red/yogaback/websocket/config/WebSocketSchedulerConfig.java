package com.red.yogaback.websocket.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class WebSocketSchedulerConfig {

    /**
     * Simple Broker의 heartbeat 처리를 위한 TaskScheduler 빈 정의.
     * ThreadPoolTaskScheduler를 사용하며, pool size와 스레드 이름을 설정합니다.
     */
    @Bean("webSocketTaskScheduler")
    public TaskScheduler webSocketTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("wss-heartbeat-");
        return scheduler;
    }
}
