package com.skillproof.backend.identity.infrastructure;

import com.skillproof.backend.identity.domain.OAuthAccount;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OAuthAccountRepository extends JpaRepository<OAuthAccount, UUID> {

  Optional<OAuthAccount> findByProviderAndProviderSubject(String provider, String providerSubject);
}
