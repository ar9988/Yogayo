package com.red.yogaback.websocket.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

import com.red.yogaback.websocket.service.UserSessionService;
import com.red.yogaback.websocket.service.WebSocketConnectionService;
import com.red.yogaback.websocket.service.UserSession;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer, ApplicationContextAware {

    private final WebSocketAuthChannelInterceptor authChannelInterceptor;
    private final StompLoggingInterceptor stompLoggingInterceptor;
    private final TaskScheduler webSocketTaskScheduler;
    private ApplicationContext applicationContext;

    @Autowired
    public WebSocketConfig(
            WebSocketAuthChannelInterceptor authChannelInterceptor,
            StompLoggingInterceptor stompLoggingInterceptor,
            @Qualifier("webSocketTaskScheduler") TaskScheduler webSocketTaskScheduler
    ) {
        this.authChannelInterceptor = authChannelInterceptor;
        this.stompLoggingInterceptor = stompLoggingInterceptor;
        this.webSocketTaskScheduler = webSocketTaskScheduler;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue")
                .setHeartbeatValue(new long[]{20000, 20000})
                .setTaskScheduler(webSocketTaskScheduler);
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(
                authChannelInterceptor,
                stompLoggingInterceptor,
                // heartbeat 프레임 감지용 인라인 인터셉터
                new ChannelInterceptor() {
                    @Override
                    public Message<?> preSend(Message<?> message, MessageChannel channel) {
                        StompHeaderAccessor accessor =
                                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                        if (accessor != null && SimpMessageType.HEARTBEAT.equals(accessor.getMessageType())) {
                            String sessionId = accessor.getSessionId();
                            // 런타임에 서비스 빈을 꺼내 사용
                            UserSessionService userSessionService =
                                    applicationContext.getBean(UserSessionService.class);
                            WebSocketConnectionService connectionService =
                                    applicationContext.getBean(WebSocketConnectionService.class);

                            UserSession session = userSessionService.getSession(sessionId);
                            if (session != null) {
                                connectionService.updateConnection(
                                        sessionId,
                                        session.getRoomId(),
                                        session.getUserId()
                                );
                            }
                        }
                        return message;
                    }
                }
        );
    }

    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompLoggingInterceptor);
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration.setSendTimeLimit(15 * 1000)
                .setSendBufferSizeLimit(512 * 1024)
                .setMessageSizeLimit(128 * 1024);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
