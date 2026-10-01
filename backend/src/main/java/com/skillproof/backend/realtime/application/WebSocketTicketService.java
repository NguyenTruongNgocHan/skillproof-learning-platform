package com.skillproof.backend.realtime.application;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.skillproof.backend.common.exception.UnauthorizedException;
import com.skillproof.backend.identity.contract.IdentityAccessQuery;

@Service
public class WebSocketTicketService {

    private final StringRedisTemplate redis;
    private final IdentityAccessQuery identity;
    private final Duration ttl;
    private final String wsUrl;
    private final SecureRandom random = new SecureRandom();

    public WebSocketTicketService(StringRedisTemplate redis, IdentityAccessQuery identity, @Value("${skillproof.realtime.ticket-ttl:60s}") Duration ttl, @Value("${skillproof.realtime.ws-public-url:ws://localhost:8080/ws/realtime}") String wsUrl) {
        this.redis = redis;
        this.identity = identity;
        this.ttl = ttl;
        this.wsUrl = wsUrl;
    }

    public Ticket issue(UUID user) {
        if (!identity.isActiveLearner(user)) {
            throw new UnauthorizedException("LEARNER_REQUIRED", "Active learner required");
        }
        byte[] b = new byte[32];
        random.nextBytes(b);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(b);
        redis.opsForValue().set("rt:ticket:" + token, user.toString(), ttl);
        return new Ticket(token, Instant.now().plus(ttl), wsUrl);
    }

    public UUID consume(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        String key = "rt:ticket:" + token;
        String value = redis.opsForValue().getAndDelete(key);
        return value == null ? null : UUID.fromString(value);
    }

    public record Ticket(String ticket, Instant expiresAt, String webSocketUrl) {

    }
}
