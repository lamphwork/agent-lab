package com.bmn.socket;

import com.bmn.model.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class SessionManager {

    private final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public void addSession(String sessionId, WebSocketSession session) {
        sessions.put(sessionId, session);
    }

    public void removeSession(String sessionId) {
        try (WebSocketSession session = sessions.remove(sessionId)) {
            log.info("Removing session {}", sessionId);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendMessage(String to, Message message) {
        Optional.ofNullable(sessions.get(to)).ifPresent(session -> {
            try {
                session.sendMessage(new TextMessage(""));
            } catch (IOException e) {
                throw new RuntimeException("Send message failure", e);
            }
        });
    }

    public void sendMessage(Set<String> to, Message message) {
        for (String sessionId : to) {
            sendMessage(sessionId, message);
        }
    }
}
