package com.aiprovider.config;

import com.fasterxml.jackson.databind.*;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SignalHandler extends TextWebSocketHandler {
    private final ObjectMapper mapper;
    private final Map<String, Set<WebSocketSession>> rooms = new ConcurrentHashMap<>();
    private final Map<String, String> sessionRooms = new ConcurrentHashMap<>();
    public SignalHandler(ObjectMapper mapper) { this.mapper = mapper; }

    @Override protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        JsonNode node = mapper.readTree(message.getPayload());
        String type = node.path("type").asText();
        if ("join".equals(type)) {
            String room = node.path("room").asText();
            if (!room.matches("[A-Za-z0-9_-]{4,64}")) { session.close(CloseStatus.BAD_DATA); return; }
            leave(session);
            Set<WebSocketSession> members = rooms.computeIfAbsent(room, key -> ConcurrentHashMap.newKeySet());
            if (members.size() >= 2) { session.sendMessage(new TextMessage("{\"type\":\"room-full\"}")); return; }
            members.add(session); sessionRooms.put(session.getId(), room);
            broadcast(room, session, "{\"type\":\"peer-joined\"}");
            session.sendMessage(new TextMessage("{\"type\":\"joined\",\"peers\":" + (members.size() - 1) + "}"));
            return;
        }
        String room = sessionRooms.get(session.getId());
        if (room == null || !new HashSet<>(Arrays.asList("offer", "answer", "ice", "stop", "state")).contains(type)) return;
        broadcast(room, session, message.getPayload());
    }
    @Override public void afterConnectionClosed(WebSocketSession s, CloseStatus status) { leave(s); }
    @Override public void handleTransportError(WebSocketSession s, Throwable error) { leave(s); }
    private void leave(WebSocketSession s) {
        String room = sessionRooms.remove(s.getId()); if (room == null) return;
        Set<WebSocketSession> members = rooms.get(room); if (members != null) { members.remove(s); if (members.isEmpty()) rooms.remove(room); }
        broadcast(room, s, "{\"type\":\"peer-left\"}");
    }
    private void broadcast(String room, WebSocketSession sender, String payload) {
        Set<WebSocketSession> members = rooms.getOrDefault(room, Collections.emptySet());
        for (WebSocketSession member : members) if (member != sender && member.isOpen()) try { member.sendMessage(new TextMessage(payload)); } catch (IOException ignored) {}
    }
}
