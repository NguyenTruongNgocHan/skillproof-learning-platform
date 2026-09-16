package com.skillproof.backend.identity.api;

import com.skillproof.backend.identity.application.RegisterUserService;
import com.skillproof.backend.identity.application.ResendEmailVerificationService;
import com.skillproof.backend.identity.application.VerifyEmailService;
import com.skillproof.backend.identity.application.AuthenticationService;
import com.skillproof.backend.identity.application.RequestMetadata;
import com.skillproof.backend.identity.application.TokenProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class IdentityController {

    private final RegisterUserService
            registerUserService;

    private final VerifyEmailService
            verifyEmailService;

    private final ResendEmailVerificationService
            resendEmailVerificationService;
    private final AuthenticationService authenticationService;
    private final TokenProperties tokenProperties;

    public IdentityController(
            RegisterUserService registerUserService,
            VerifyEmailService verifyEmailService,
            ResendEmailVerificationService resendEmailVerificationService,
            AuthenticationService authenticationService,
            TokenProperties tokenProperties
    ) {
        this.registerUserService =
                registerUserService;

        this.verifyEmailService =
                verifyEmailService;

        this.resendEmailVerificationService =
                resendEmailVerificationService;
        this.authenticationService = authenticationService;
        this.tokenProperties = tokenProperties;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @Valid
            @RequestBody
            RegisterRequest request
    ) {

        RegisterResponse response =
                registerUserService.register(
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/verify-email")
    public ResponseEntity<VerifyEmailResponse>
    verifyEmail(
            @Valid
            @RequestBody
            VerifyEmailRequest request
    ) {

        return ResponseEntity.ok(
                verifyEmailService.verify(
                        request
                )
        );
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<Void>
    resendVerification(
            @Valid
            @RequestBody
            ResendVerificationRequest request
    ) {

        resendEmailVerificationService.resend(
                request
        );

        return ResponseEntity
                .accepted()
                .build();
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request,
                              HttpServletRequest servletRequest,
                              HttpServletResponse servletResponse) {
        var issued = authenticationService.login(request, metadata(servletRequest));
        setRefreshCookie(servletResponse, issued.refreshToken());
        return issued.response();
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(HttpServletRequest request, HttpServletResponse response) {
        var issued = authenticationService.refresh(refreshCookie(request), metadata(request));
        setRefreshCookie(response, issued.refreshToken());
        return issued.response();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        authenticationService.logout(refreshCookie(request));
        clearRefreshCookie(response);
        return ResponseEntity.noContent().build();
    }

    private RequestMetadata metadata(HttpServletRequest request) {
        return new RequestMetadata(request.getRemoteAddr(), request.getHeader("User-Agent"));
    }

    private String refreshCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if ("skillproof_refresh".equals(cookie.getName())) return cookie.getValue();
        }
        return null;
    }

    private void setRefreshCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("skillproof_refresh", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(tokenProperties.isSecureCookie());
        cookie.setPath("/api/v1/auth");
        cookie.setMaxAge((int) tokenProperties.getRefreshTtl().toSeconds());
        cookie.setAttribute("SameSite", "Lax");
        response.addCookie(cookie);
    }

    private void clearRefreshCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("skillproof_refresh", "");
        cookie.setHttpOnly(true);
        cookie.setSecure(tokenProperties.isSecureCookie());
        cookie.setPath("/api/v1/auth");
        cookie.setMaxAge(0);
        cookie.setAttribute("SameSite", "Lax");
        response.addCookie(cookie);
    }
}
