package xyz.haimianxiaozi.websocket;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import xyz.haimianxiaozi.security.AuthCookieManager;
import xyz.haimianxiaozi.security.TokenBlacklistService;
import xyz.haimianxiaozi.util.JwtUtil;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class AuthenticatedHandshakeInterceptor implements HandshakeInterceptor {

    private final AuthCookieManager authCookieManager;
    private final TokenBlacklistService tokenBlacklistService;
    private final JwtUtil jwtUtil;

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes) {
        if (!(request instanceof ServletServerHttpRequest servletRequest)) {
            return false;
        }
        HttpServletRequest httpRequest = servletRequest.getServletRequest();
        String token = authCookieManager.readToken(httpRequest).orElse(null);
        Long userId = jwtUtil.getUserIdSafely(token);
        if (userId == null || tokenBlacklistService.isBlacklisted(token)) {
            return false;
        }
        attributes.put(AuthenticatedWebSocketHandler.USER_ID_ATTRIBUTE, userId);
        return true;
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception) {
    }
}