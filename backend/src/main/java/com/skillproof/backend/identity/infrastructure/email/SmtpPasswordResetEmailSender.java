package com.skillproof.backend.identity.infrastructure.email;

import com.skillproof.backend.identity.application.PasswordResetProperties;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

@Component
public class SmtpPasswordResetEmailSender implements PasswordResetEmailSender {

    private final JavaMailSender mailSender;
    private final PasswordResetProperties properties;

    public SmtpPasswordResetEmailSender(
            JavaMailSender mailSender,
            PasswordResetProperties properties
    ) {
        this.mailSender = mailSender;
        this.properties = properties;
    }

    @Override
    public void sendPasswordResetEmail(
            String email,
            String displayName,
            String rawToken,
            Instant expiresAt
    ) {
        String resetUrl = properties.getFrontendResetUrl()
                + "?token="
                + URLEncoder.encode(rawToken, StandardCharsets.UTF_8);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(properties.getFromAddress());
        message.setTo(email);
        message.setSubject("Reset your SkillProof password");
        message.setText("""
                Hi %s,

                We received a request to reset your SkillProof password.

                Open the secure link below to choose a new password:

                %s

                This link expires at:
                %s

                If you did not request a password reset, you can ignore this email.

                SkillProof
                Learn. Play. Get Certified.
                """.formatted(displayName, resetUrl, expiresAt));
        mailSender.send(message);
    }
}
