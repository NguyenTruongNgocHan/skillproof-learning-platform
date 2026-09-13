package com.skillproof.backend.identity.application;

import com.skillproof.backend.common.exception.ConflictException;
import com.skillproof.backend.identity.api.RegisterRequest;
import com.skillproof.backend.identity.api.RegisterResponse;
import com.skillproof.backend.identity.domain.UserAccount;
import com.skillproof.backend.identity.infrastructure.UserAccountRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class RegisterUserService {

    private static final String EMAIL_ALREADY_EXISTS_CODE =
            "IDENTITY_EMAIL_ALREADY_EXISTS";

    private static final String EMAIL_ALREADY_EXISTS_MESSAGE =
            "An account with this email already exists.";

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisterUserService(
            UserAccountRepository userAccountRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {

        String normalizedEmail =
                normalizeEmail(request.email());

        ensureEmailAvailable(normalizedEmail);

        String passwordHash =
                passwordEncoder.encode(request.password());

        UserAccount account =
                UserAccount.newLearner(
                        normalizedEmail,
                        passwordHash
                );

        UserAccount savedAccount;

        try {
            /*
             * flush is intentional.
             *
             * The database UNIQUE(email) constraint is the final
             * protection against concurrent registrations using
             * the same normalized email.
             */
            savedAccount =
                    userAccountRepository.saveAndFlush(account);

        } catch (DataIntegrityViolationException exception) {

            throw emailAlreadyExists();
        }

        return toResponse(savedAccount);
    }

    private void ensureEmailAvailable(String normalizedEmail) {

        if (userAccountRepository.existsByEmail(normalizedEmail)) {
            throw emailAlreadyExists();
        }
    }

    private ConflictException emailAlreadyExists() {

        return new ConflictException(
                EMAIL_ALREADY_EXISTS_CODE,
                EMAIL_ALREADY_EXISTS_MESSAGE
        );
    }

    private RegisterResponse toResponse(UserAccount account) {

        return new RegisterResponse(
                account.getId(),
                account.getEmail(),
                account.getRole(),
                account.getStatus(),
                account.getCreatedAt()
        );
    }

    private String normalizeEmail(String email) {

        return email
                .trim()
                .toLowerCase(Locale.ROOT);
    }
}