package com.skillproof.backend.identity.application;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.skillproof.backend.identity.domain.UserAccount;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Component
public class TokenCodec {

    private final TokenProperties properties;
    private final Algorithm algorithm;
    private final SecureRandom random = new SecureRandom();

    public TokenCodec(TokenProperties properties) {
        this.properties = properties;
        if (properties.getSecret() == null || properties.getSecret().length() < 32) {
            throw new IllegalStateException("JWT_SECRET must contain at least 32 characters.");
        }
        this.algorithm = Algorithm.HMAC256(properties.getSecret());
    }

    public String accessToken(UserAccount user, Instant now) {
        return JWT.create()
                .withIssuer(properties.getIssuer())
                .withSubject(user.getId().toString())
                .withClaim("role", user.getRole().name())
                .withIssuedAt(now)
                .withExpiresAt(now.plus(properties.getAccessTtl()))
                .withJWTId(UUID.randomUUID().toString())
                .sign(algorithm);
    }

    public DecodedJWT verifyAccessToken(String token) {
        return JWT.require(algorithm).withIssuer(properties.getIssuer()).build().verify(token);
    }

    public String randomRefreshToken() {
        byte[] bytes = new byte[48];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public String hash(String token) {
        try {
            return java.util.HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8))
            );
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable.", exception);
        }
    }
}
