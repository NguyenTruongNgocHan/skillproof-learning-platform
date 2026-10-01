package com.skillproof.backend.realtime.infrastructure;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.skillproof.backend.realtime.application.BattleCoordinator;
import com.skillproof.backend.realtime.application.BattleRepository;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
public class RealtimeWebSocketHandler extends TextWebSocketHandler implements BattleCoordinator.Events {

    private final ObjectMapper json;
    private final BattleCoordinator battles;
    private final BattleRepository repository;
    private final Map<UUID, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public RealtimeWebSocketHandler(ObjectMapper json, BattleCoordinator battles, BattleRepository repository) {
        this.json = json;
        this.battles = battles;
        this.repository = repository;
        battles.events(this);
    }

    private UUID user(WebSocketSession s) {
        return (UUID) s.getAttributes().get("userId");
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession s) {
        sessions.put(user(s), s);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession s, CloseStatus status) {
        sessions.remove(user(s), s);
    }

    @Override
    protected void handleTextMessage(WebSocketSession s, TextMessage m) throws Exception {
        UUID u = user(s);
        JsonNode root = json.readTree(m.getPayload());
        String type = root.path("type").asText();
        JsonNode p = root.path("payload");
        try {
            switch (type) {
                case "queue.join" ->
                    battles.join(u, context(p));
                case "queue.leave" ->
                    battles.leave(u);
                case "room.create" -> {
                    String code = battles.createPrivate(u, context(p));
                    user(u, "room.created", null, 0, Map.of("code", code, "skillContext", context(p)));
                }
                case "room.join" ->
                    battles.joinPrivate(u, p.path("code").asText());
                case "battle.ready" ->
                    battles.ready(u, UUID.fromString(p.path("battleId").asText()));
                case "battle.answer.submit" ->
                    battles.answer(u, UUID.fromString(p.path("battleId").asText()), UUID.fromString(p.path("questionId").asText()), UUID.fromString(p.path("submissionId").asText()), UUID.fromString(p.path("optionId").asText()));
                case "battle.snapshot.request" ->
                    battles.snapshot(u, UUID.fromString(p.path("battleId").asText()));
                default ->
                    error(u, root.path("requestId").asText(null), "WS_PROTOCOL_INVALID", "Unknown command");
            }
        } catch (RuntimeException ex) {
            error(u, root.path("requestId").asText(null), code(ex), ex.getMessage());
        }
    }

    private String context(JsonNode p) {
        String x = p.path("skillId").asText();
        if (x.isBlank()) {
            x = p.path("domainId").asText();
        }
        if (x.isBlank()) {
            x = p.path("skillContext").asText();
        }
        if (x.isBlank()) {
            throw new IllegalArgumentException("skill/domain context required");
        }
        return x;
    }

    private String code(Throwable e) {
        try {
            return (String) e.getClass().getMethod("getCode").invoke(e);
        } catch (Exception ignored) {
            return "REALTIME_COMMAND_REJECTED";
        }
    }

    private void error(UUID u, String request, String code, String detail) {
        user(u, "error", null, 0, Map.of("code", code, "detail", detail == null ? "Rejected" : detail, "correlationId", request == null ? "" : request));
    }

    public void user(UUID u, String type, UUID battle, long seq, Object payload) {
        WebSocketSession s = sessions.get(u);
        if (s != null && s.isOpen()) {
            send(s, type, battle, seq, payload);
        }
    }

    public void battle(UUID battle, String type, long seq, Object payload) {
        for (var e : sessions.entrySet()) {
            if (repository.participates(battle, e.getKey())) {
                send(e.getValue(), type, battle, seq, payload);
            }
        }
    }

    private synchronized void send(WebSocketSession s, String type, UUID battle, long seq, Object payload) {
        try {
            Map<String, Object> env = new LinkedHashMap<>();
            env.put("type", type);
            env.put("eventId", UUID.randomUUID());
            if (battle != null) {
                env.put("battleId", battle);
            }
            if (seq > 0) {
                env.put("sequence", seq);
            }
            env.put("serverTime", Instant.now());
            env.put("payload", payload);
            s.sendMessage(new TextMessage(json.writeValueAsString(env)));
        } catch (Exception ignored) {
        }
    }
}
