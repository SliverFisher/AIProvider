package com.aiprovider.config;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;
@Configuration @EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    private final SignalHandler handler;
    public WebSocketConfig(SignalHandler handler) { this.handler = handler; }
    @Override public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(handler, "/ws/signal").setAllowedOrigins();
    }
}
