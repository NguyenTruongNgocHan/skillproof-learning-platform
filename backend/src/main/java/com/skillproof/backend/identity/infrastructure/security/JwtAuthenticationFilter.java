package com.skillproof.backend.identity.infrastructure.security;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.skillproof.backend.identity.application.TokenCodec;
import com.skillproof.backend.identity.domain.AccountStatus;
import com.skillproof.backend.identity.infrastructure.AuthSessionRepository;
import com.skillproof.backend.identity.infrastructure.UserAccountRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenCodec tokens;
    private final UserAccountRepository users;
    private final AuthSessionRepository sessions;

    public JwtAuthenticationFilter(
            TokenCodec tokens,
            UserAccountRepository users,
            AuthSessionRepository sessions
    ) {
        this.tokens = tokens;
        this.users = users;
        this.sessions = sessions;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain
    ) throws ServletException, IOException {

        String authorization = request.getHeader("Authorization");
        if (request.getRequestURI().startsWith("/api/")) {
            SecurityContextHolder.clearContext();
        }

        if (authorization != null && authorization.startsWith("Bearer ")) {
            try {
                var jwt = tokens.verifyAccessToken(
                        authorization.substring(7)
                );

                String subject = jwt.getSubject();
                if (subject == null || subject.isBlank()) {
                    throw new IllegalArgumentException("Subject required");
                }
                UUID userId = UUID.fromString(subject);
                String sessionClaim = jwt.getClaim("sessionId").asString();
                if (sessionClaim == null || sessionClaim.isBlank()) {
                    throw new IllegalArgumentException("Session claim required");
                }
                UUID sessionId = UUID.fromString(sessionClaim);

                users.findById(userId)
                        .filter(user -> user.getStatus() == AccountStatus.ACTIVE)
                        .filter(user -> sessions.findById(sessionId)
                        .filter(session -> session.getUserAccountId().equals(userId))
                        .filter(session -> session.isUsable(java.time.Instant.now()))
                        .isPresent())
                        .ifPresent(user -> {
                            var authority = new SimpleGrantedAuthority(
                                    "ROLE_" + user.getRole().name()
                            );

                            var authentication
                                    = new UsernamePasswordAuthenticationToken(
                                            userId,
                                            null,
                                            List.of(authority)
                                    );

                            SecurityContextHolder
                                    .getContext()
                                    .setAuthentication(authentication);
                        });

            } catch (JWTVerificationException | IllegalArgumentException ignored) {
                SecurityContextHolder.clearContext();
            }
        }

        chain.doFilter(request, response);
    }
}
