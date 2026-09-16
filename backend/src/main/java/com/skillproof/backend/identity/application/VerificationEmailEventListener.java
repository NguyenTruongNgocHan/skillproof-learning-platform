package com.skillproof.backend.identity.application;

import com.skillproof.backend.identity.infrastructure.email.VerificationEmailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class VerificationEmailEventListener {

    private static final Logger log =
            LoggerFactory.getLogger(
                    VerificationEmailEventListener.class
            );

    private final VerificationEmailSender emailSender;

    public VerificationEmailEventListener(
            VerificationEmailSender emailSender
    ) {
        this.emailSender = emailSender;
    }

    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void handle(
            VerificationEmailRequestedEvent event
    ) {

        try {

            emailSender.sendVerificationEmail(
                    event.email(),
                    event.displayName(),
                    event.rawToken(),
                    event.expiresAt()
            );

        } catch (MailException exception) {

            log.error(
                    "Verification email delivery failed for {}",
                    maskEmail(event.email()),
                    exception
            );

        } catch (RuntimeException exception) {

            log.error(
                    "Unexpected verification email delivery failure for {}",
                    maskEmail(event.email()),
                    exception
            );
        }
    }

    private String maskEmail(String email) {

        int atIndex = email.indexOf('@');

        if (atIndex <= 1) {
            return "***";
        }

        return email.charAt(0)
                + "***"
                + email.substring(atIndex);
    }
}