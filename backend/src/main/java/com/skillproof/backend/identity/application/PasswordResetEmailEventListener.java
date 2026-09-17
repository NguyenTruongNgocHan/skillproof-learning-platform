package com.skillproof.backend.identity.application;

import com.skillproof.backend.identity.infrastructure.email.PasswordResetEmailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class PasswordResetEmailEventListener {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetEmailEventListener.class);
    private final PasswordResetEmailSender emailSender;

    public PasswordResetEmailEventListener(PasswordResetEmailSender emailSender) {
        this.emailSender = emailSender;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(PasswordResetEmailRequestedEvent event) {
        try {
            emailSender.sendPasswordResetEmail(
                    event.email(),
                    event.displayName(),
                    event.rawToken(),
                    event.expiresAt()
            );
        } catch (RuntimeException exception) {
            log.error(
                    "Password reset email delivery failed for {}",
                    maskEmail(event.email()),
                    exception
            );
        }
    }

    private String maskEmail(String email) {
        int atIndex = email.indexOf('@');
        return atIndex <= 1 ? "***" : email.charAt(0) + "***" + email.substring(atIndex);
    }
}
