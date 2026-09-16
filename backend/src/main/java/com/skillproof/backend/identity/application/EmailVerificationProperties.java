package com.skillproof.backend.identity.application;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@ConfigurationProperties(
        prefix = "skillproof.identity.email-verification"
)
public class EmailVerificationProperties {

    private Duration ttl
            = Duration.ofMinutes(30);

    private Duration resendCooldown
            = Duration.ofSeconds(60);

    private String frontendVerificationUrl
            = "http://localhost:5173/verify-email";

    private String fromAddress
            = "no-reply@skillproof.local";

    public Duration getTtl() {
        return ttl;
    }

    public void setTtl(Duration ttl) {
        this.ttl = ttl;
    }

    public Duration getResendCooldown() {
        return resendCooldown;
    }

    public void setResendCooldown(
            Duration resendCooldown
    ) {
        this.resendCooldown = resendCooldown;
    }

    public String getFrontendVerificationUrl() {
        return frontendVerificationUrl;
    }

    public void setFrontendVerificationUrl(
            String frontendVerificationUrl
    ) {
        this.frontendVerificationUrl
                = frontendVerificationUrl;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(
            String fromAddress
    ) {
        this.fromAddress = fromAddress;
    }
}
