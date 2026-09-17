package com.skillproof.backend.identity.application;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@ConfigurationProperties(prefix = "skillproof.identity.password-reset")
public class PasswordResetProperties {

    private Duration ttl = Duration.ofMinutes(30);
    private Duration requestCooldown = Duration.ofSeconds(60);
    private String frontendResetUrl = "http://localhost:5173/reset-password";
    private String fromAddress = "no-reply@skillproof.local";

    public Duration getTtl() {
        return ttl;
    }

    public void setTtl(Duration ttl) {
        this.ttl = ttl;
    }

    public String getFrontendResetUrl() {
        return frontendResetUrl;
    }

    public Duration getRequestCooldown() {
        return requestCooldown;
    }

    public void setRequestCooldown(Duration requestCooldown) {
        this.requestCooldown = requestCooldown;
    }

    public void setFrontendResetUrl(String frontendResetUrl) {
        this.frontendResetUrl = frontendResetUrl;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }
}
