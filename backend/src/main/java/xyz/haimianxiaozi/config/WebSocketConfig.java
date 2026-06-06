package xyz.haimianxiaozi.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import xyz.haimianxiaozi.websocket.AuthenticatedHandshakeInterceptor;
import xyz.haimianxiaozi.websocket.AuthenticatedWebSocketHandler;
import xyz.haimianxiaozi.websocket.WebSocketSessionRegistry;

import java.util.List;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final AuthenticatedHandshakeInterceptor handshakeInterceptor;
    private final WebSocketSessionRegistry sessionRegistry;

    @Value("${security.cors.allowed-origins}")
    private List<String> allowedOrigins;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new AuthenticatedWebSocketHandler(sessionRegistry), "/api/ws")
                .addInterceptors(handshakeInterceptor)
                .setAllowedOrigins(allowedOrigins.toArray(String[]::new));
    }
}