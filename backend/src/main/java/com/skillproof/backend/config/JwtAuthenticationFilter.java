package com.skillproof.backend.config;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.skillproof.backend.identity.application.TokenCodec;
import com.skillproof.backend.identity.domain.AccountStatus;
import com.skillproof.backend.identity.infrastructure.UserAccountRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final TokenCodec tokens;
    private final UserAccountRepository users;

    public JwtAuthenticationFilter(TokenCodec tokens, UserAccountRepository users) {
        this.tokens = tokens;
        this.users = users;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            try {
                var jwt = tokens.verifyAccessToken(authorization.substring(7));
                UUID userId = UUID.fromString(jwt.getSubject());
                users.findById(userId).filter(user -> user.getStatus() == AccountStatus.ACTIVE).ifPresent(user -> {
                    var authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().name());
                    var authentication = new UsernamePasswordAuthenticationToken(userId, null, List.of(authority));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                });
            } catch (JWTVerificationException | IllegalArgumentException ignored) {
                SecurityContextHolder.clearContext();
            }
        }
        chain.doFilter(request, response);
    }
}
