package com.skillproof.backend.realtime.infrastructure;

import java.util.Map;
import java.util.UUID;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.skillproof.backend.realtime.application.WebSocketTicketService;

@Component
public class RealtimeHandshakeInterceptor implements HandshakeInterceptor {

    private final WebSocketTicketService tickets;

    public RealtimeHandshakeInterceptor(WebSocketTicketService tickets) {
        this.tickets = tickets;
    }

    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler handler, Map<String, Object> attrs) {
        String q = request.getURI().getQuery();
        String ticket = null;
        if (q != null) {
            for (String p : q.split("&")) {
                String[] kv = p.split("=", 2);
                if (kv.length == 2 && kv[0].equals("ticket")) {
                    ticket = java.net.URLDecoder.decode(kv[1], java.nio.charset.StandardCharsets.UTF_8);
                }
            }
        }
        UUID user = tickets.consume(ticket);
        if (user == null) {
            return false;
        }
        attrs.put("userId", user);
        return true;
    }

    public void afterHandshake(ServerHttpRequest r, ServerHttpResponse s, WebSocketHandler h, Exception e) {
    }
}
