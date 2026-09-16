package com.skillproof.backend.identity.infrastructure;

import com.skillproof.backend.identity.domain.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface EmailVerificationTokenRepository
        extends JpaRepository<EmailVerificationToken, UUID> {

    Optional<EmailVerificationToken> findByTokenHash(
            String tokenHash
    );

    Optional<EmailVerificationToken>
    findTopByUserAccountIdOrderByCreatedAtDesc(
            UUID userAccountId
    );

    @Modifying(
            flushAutomatically = true,
            clearAutomatically = true
    )
    @Query("""
            update EmailVerificationToken t
               set t.invalidatedAt = :now
             where t.userAccountId = :userAccountId
               and t.consumedAt is null
               and t.invalidatedAt is null
            """)
    int invalidateOutstandingTokens(
            @Param("userAccountId") UUID userAccountId,
            @Param("now") Instant now
    );

    @Modifying(
            flushAutomatically = true,
            clearAutomatically = true
    )
    @Query("""
            update EmailVerificationToken t
               set t.consumedAt = :now
             where t.id = :tokenId
               and t.consumedAt is null
               and t.invalidatedAt is null
               and t.expiresAt > :now
            """)
    int consumeIfUsable(
            @Param("tokenId") UUID tokenId,
            @Param("now") Instant now
    );
}