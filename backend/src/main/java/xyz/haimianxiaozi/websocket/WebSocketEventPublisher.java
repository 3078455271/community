package xyz.haimianxiaozi.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class WebSocketEventPublisher {

    private final WebSocketSessionRegistry sessionRegistry;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public void publish(Long userId, String type, Object payload) {
        WebSocketEvent event = new WebSocketEvent(type, payload);
        for (WebSocketSession session : sessionRegistry.getSessions(userId)) {
            if (!session.isOpen()) {
                continue;
            }
            try {
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(event)));
            } catch (IOException e) {
                closeQuietly(userId, session);
            }
        }
    }

    public void publishUnreadCount(Long userId, String type, long count) {
        publish(userId, type, Map.of("count", count));
    }

    private void closeQuietly(Long userId, WebSocketSession session) {
        try {
            sessionRegistry.close(userId, session);
        } catch (IOException ignored) {
            sessionRegistry.unregister(userId, session);
        }
    }

    public record WebSocketEvent(String type, Object payload) {
    }
}