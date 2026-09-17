package com.skillproof.backend.identity.infrastructure;

import com.skillproof.backend.identity.domain.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {
}
