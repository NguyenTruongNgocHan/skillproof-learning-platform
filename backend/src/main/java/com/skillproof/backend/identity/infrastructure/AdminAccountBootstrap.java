package com.skillproof.backend.identity.infrastructure;

import com.skillproof.backend.identity.domain.UserAccount;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;

@Component
public class AdminAccountBootstrap implements ApplicationRunner {

    private final UserAccountRepository users;
    private final PasswordEncoder passwords;
    private final String email;
    private final String password;

    public AdminAccountBootstrap(UserAccountRepository users, PasswordEncoder passwords,
            @Value("${skillproof.bootstrap.admin.email:}") String email,
            @Value("${skillproof.bootstrap.admin.password:}") String password) {
        this.users = users;
        this.passwords = passwords;
        this.email = email;
        this.password = password;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (email.isBlank() && password.isBlank()) {
            return;
        }
        if (email.isBlank() || password.length() < 12) {
            throw new IllegalStateException("ADMIN_EMAIL and a strong ADMIN_PASSWORD are both required.");
        }
        String normalized = email.trim().toLowerCase(Locale.ROOT);
        if (!users.existsByEmail(normalized)) {
            users.save(UserAccount.newAdmin(normalized, passwords.encode(password), "SkillProof Admin", Instant.now()));
        }
    }
}
