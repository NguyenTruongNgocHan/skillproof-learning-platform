package com.skillproof.backend.config;

import com.skillproof.backend.identity.application.OAuthLoginService;
import com.skillproof.backend.identity.application.RequestMetadata;
import com.skillproof.backend.identity.application.TokenProperties;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
    private final OAuthLoginService oauth;
    private final TokenProperties tokens;
    private final String frontendOrigin;

    public OAuth2LoginSuccessHandler(OAuthLoginService oauth, TokenProperties tokens,
                                     @Value("${skillproof.cors.allowed-origin}") String frontendOrigin) {
        this.oauth = oauth;
        this.tokens = tokens;
        this.frontendOrigin = frontendOrigin;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        var oauthToken = (OAuth2AuthenticationToken) authentication;
        OAuth2User principal = oauthToken.getPrincipal();
        var issued = oauth.login(oauthToken.getAuthorizedClientRegistrationId(), principal.getName(),
                principal.getAttribute("email"), principal.getAttribute("name"),
                Boolean.TRUE.equals(principal.getAttribute("email_verified")),
                new RequestMetadata(request.getRemoteAddr(), request.getHeader("User-Agent")));
        Cookie cookie = new Cookie("skillproof_refresh", issued.refreshToken());
        cookie.setHttpOnly(true);
        cookie.setSecure(tokens.isSecureCookie());
        cookie.setPath("/api/v1/auth");
        cookie.setMaxAge((int) tokens.getRefreshTtl().toSeconds());
        cookie.setAttribute("SameSite", "Lax");
        response.addCookie(cookie);
        response.sendRedirect(frontendOrigin + "/oauth/callback");
    }
}
