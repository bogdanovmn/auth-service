package com.github.bogdanovmn.authservice.common.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AccountOAuthIdentityRepository extends JpaRepository<AccountOAuthIdentity, UUID> {
	Optional<AccountOAuthIdentity> findByProviderAndProviderUserId(String provider, String providerUserId);
}
