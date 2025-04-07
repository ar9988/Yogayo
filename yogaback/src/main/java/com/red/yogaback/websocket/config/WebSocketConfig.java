//Spring WebSocket 메시지 브로커 설정을 담당하며, STOMP 엔드포인트 등록, 메시지 브로커 설정,
// 그리고 클라이언트 인바운드/아웃바운드 채널에 인터셉터를 등록합니다.
package com.red.yogaback.websocket.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.beans.factory.annotation.Qualifier;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    // WebSocket 연결 시 인증 및 로깅 인터셉터를 주입받음
    private final WebSocketAuthChannelInterceptor authChannelInterceptor;
    private final StompLoggingInterceptor stompLoggingInterceptor;

    // 생성자 주입을 통해 인터셉터들을 할당
    @Autowired
    public WebSocketConfig(WebSocketAuthChannelInterceptor authChannelInterceptor,
                           StompLoggingInterceptor stompLoggingInterceptor) {
        this.authChannelInterceptor = authChannelInterceptor;
        this.stompLoggingInterceptor = stompLoggingInterceptor;
    }

    @Autowired
    @Qualifier("webSocketTaskScheduler")
    private TaskScheduler webSocketTaskScheduler;

    /**
     * 메시지 브로커 관련 설정.
     * 개선방향:
     *  - 추후 복잡한 메시지 라우팅이나 클러스터링 등 확장이 필요한 경우 별도의 브로커 설정을 고려할 수 있음.
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue")
            .setHeartbeatValue(new long[]{10000, 10000})
            .setTaskScheduler(webSocketTaskScheduler);
        config.setApplicationDestinationPrefixes("/app");
    }

    /**
     * STOMP 엔드포인트 등록.
     * 개선방향:
     *  - CORS 정책을 더 세밀하게 제어할 수 있음 (현재는 모든 출처를 허용).
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // "/ws" 엔드포인트에 대해 STOMP 연결을 허용하고, 모든 출처에 대해 접근 허용
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    /**
     * 클라이언트에서 서버로 들어오는 메시지 채널에 인터셉터를 추가.
     * 개선방향:
     *  - 여러 인터셉터를 체인으로 구성하여, 향후 추가적인 처리나 인증 로직 확장이 용이함.
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // 인증 인터셉터와 로깅 인터셉터를 함께 등록
        registration.interceptors(authChannelInterceptor, stompLoggingInterceptor);
    }

    /**
     * 클라이언트로 나가는 메시지 채널에 인터셉터를 추가.
     * 개선방향:
     *  - 필요한 경우, 아웃바운드 메시지 처리 로직 추가를 고려할 수 있음.
     */
    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        // 로깅 인터셉터를 등록하여 나가는 메시지도 기록함
        registration.interceptors(stompLoggingInterceptor);
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        // 다음과 같은 설정을 추가하면 좋습니다
        registration.setSendTimeLimit(15 * 1000)
                   .setSendBufferSizeLimit(512 * 1024)
                   .setMessageSizeLimit(128 * 1024);
    }
}
