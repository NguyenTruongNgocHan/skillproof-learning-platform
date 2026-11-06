package com.skillproof.backend.identity.infrastructure;

import com.skillproof.backend.identity.domain.UserProfile;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {}
