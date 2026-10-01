package com.skillproof.backend.identity.infrastructure;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.skillproof.backend.identity.domain.UserProfile;

public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {
}
