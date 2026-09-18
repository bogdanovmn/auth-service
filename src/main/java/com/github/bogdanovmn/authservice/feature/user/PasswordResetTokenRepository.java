package com.github.bogdanovmn.authservice.feature.user;

import com.github.bogdanovmn.authservice.common.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {
	void deleteByAccount(Account account);
}