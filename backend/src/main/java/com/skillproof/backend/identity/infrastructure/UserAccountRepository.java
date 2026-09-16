package com.skillproof.backend.identity.infrastructure;

import com.skillproof.backend.identity.domain.UserAccount;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserAccountRepository
        extends JpaRepository<UserAccount, UUID> {

    boolean existsByEmail(String email);

    Optional<UserAccount> findByEmail(String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select u
            from UserAccount u
            where u.email = :email
            """)
    Optional<UserAccount> findByEmailForUpdate(
            @Param("email") String email
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select u
            from UserAccount u
            where u.id = :id
            """)
    Optional<UserAccount> findByIdForUpdate(
            @Param("id") UUID id
    );
}