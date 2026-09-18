package com.github.bogdanovmn.authservice.feature.token;

import com.github.bogdanovmn.authservice.common.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
	Optional<RefreshToken> getByAccount(Account account);
}
