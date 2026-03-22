package com.example.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatHandler extends TextWebSocketHandler {

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String user = getUser(session);
        sessions.put(user, session);
        log.info("Connected: {}", user);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) {
        String user = getUser(session);
        sessions.remove(user);
        log.info("Disconnected: {}", user);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Map<String, Object> data = objectMapper.readValue(message.getPayload(), Map.class);

        String to = (String) data.get("to");
        WebSocketSession target = sessions.get(to);

        if (target != null && target.isOpen()) {
            target.sendMessage(new TextMessage(message.getPayload()));
        }
    }

    private String getUser(WebSocketSession session) {
        String query = session.getUri().getQuery();
        if (query == null || !query.startsWith("user=")) {
            throw new IllegalArgumentException("User query param is missing");
        }
        return query.substring("user=".length());
    }
}
