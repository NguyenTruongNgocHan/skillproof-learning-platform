package com.skillproof.backend.realtime.infrastructure;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class RealtimeWebSocketConfig implements WebSocketConfigurer {

    private final RealtimeWebSocketHandler handler;
    private final RealtimeHandshakeInterceptor auth;
    private final String path;

    public RealtimeWebSocketConfig(RealtimeWebSocketHandler handler, RealtimeHandshakeInterceptor auth, @Value("${skillproof.realtime.ws-path:/ws/realtime}") String path) {
        this.handler = handler;
        this.auth = auth;
        this.path = path;
    }

    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(handler, path).addInterceptors(auth).setAllowedOrigins("*");
    }
}
