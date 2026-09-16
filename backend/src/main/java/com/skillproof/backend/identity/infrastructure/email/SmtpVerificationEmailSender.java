package com.skillproof.backend.identity.infrastructure.email;

import com.skillproof.backend.identity.application.EmailVerificationProperties;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

@Component
public class SmtpVerificationEmailSender
        implements VerificationEmailSender {

    private final JavaMailSender mailSender;
    private final EmailVerificationProperties properties;

    public SmtpVerificationEmailSender(
            JavaMailSender mailSender,
            EmailVerificationProperties properties
    ) {
        this.mailSender = mailSender;
        this.properties = properties;
    }

    @Override
    public void sendVerificationEmail(
            String email,
            String displayName,
            String rawToken,
            Instant expiresAt
    ) {

        String encodedToken
                = URLEncoder.encode(
                        rawToken,
                        StandardCharsets.UTF_8
                );

        String verificationUrl
                = properties.getFrontendVerificationUrl()
                + "?token="
                + encodedToken;

        SimpleMailMessage message
                = new SimpleMailMessage();

        message.setFrom(
                properties.getFromAddress()
        );

        message.setTo(email);

        message.setSubject(
                "Verify your SkillProof email"
        );

        message.setText(
                """
                Hi %s,

                Welcome to SkillProof.

                Please verify your email address by opening the link below:

                %s

                This verification link expires at:
                %s

                If you did not create this account, you can ignore this email.

                SkillProof
                Learn. Play. Get Certified.
                """
                        .formatted(
                                displayName,
                                verificationUrl,
                                expiresAt
                        )
        );

        mailSender.send(message);
    }
}
