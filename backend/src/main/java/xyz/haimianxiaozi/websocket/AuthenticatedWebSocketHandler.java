package xyz.haimianxiaozi.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@RequiredArgsConstructor
public class AuthenticatedWebSocketHandler extends TextWebSocketHandler {

    public static final String USER_ID_ATTRIBUTE = "userId";

    private final WebSocketSessionRegistry sessionRegistry;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = getUserId(session);
        if (userId == null) {
            return;
        }
        sessionRegistry.register(userId, session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = getUserId(session);
        if (userId == null) {
            return;
        }
        sessionRegistry.unregister(userId, session);
    }

    private Long getUserId(WebSocketSession session) {
        Object userId = session.getAttributes().get(USER_ID_ATTRIBUTE);
        return userId instanceof Long value ? value : null;
    }
}