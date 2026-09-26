package com.github.bogdanovmn.authservice.feature.user;

import com.github.bogdanovmn.authservice.common.domain.Account;
import com.github.bogdanovmn.authservice.common.domain.AccountRepository;
import com.github.bogdanovmn.authservice.common.domain.AccountSecurityEventType;
import com.github.bogdanovmn.authservice.feature.token.JwtService;
import com.github.bogdanovmn.authservice.infrastructure.audit.SecurityEventLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@EnableConfigurationProperties(PasswordResetProperties.class)
@RequiredArgsConstructor
class PasswordResetLinkService {
	private final PasswordResetTokenRepository passwordResetTokenRepository;
	private final AccountRepository accountRepository;
	private final JwtService jwtService;
	private final SecurityEventLogger securityEventLogger;
	private final PasswordResetProperties properties;

	@Transactional
	public PasswordResetToken create(Account account) {
		passwordResetTokenRepository.deleteByAccount(account);
		passwordResetTokenRepository.flush();

		return passwordResetTokenRepository.save(
			new PasswordResetToken()
				.setAccount(account)
				.setExpiresAt(
					new Date(System.currentTimeMillis() + properties.getTtlInHours() * 3600_000)
				)
		);
	}

	long ttlInMinutes() {
		return properties.getTtlInHours() * 60;
	}

	@Transactional
	public void applyNewPassword(UUID tokenId, String password) {
		PasswordResetToken token = passwordResetTokenRepository.findById(tokenId)
			.orElseThrow(
				() -> new NoSuchElementException("Unknown password reset token")
			);

		if (token.getExpiresAt().before(new Date())) {
			throw new IllegalArgumentException("The password reset link has expired");
		}

		Account account = token.getAccount();
		account.setEncodedPassword(
			PasswordEncoderFactories.createDelegatingPasswordEncoder().encode(password)
		);
		account.setPasswordChangedAt(new Date());
		accountRepository.save(account);

		passwordResetTokenRepository.delete(token);
		passwordResetTokenRepository.flush();

		securityEventLogger.log(account.getId(), AccountSecurityEventType.PASSWORD_CHANGED);
		jwtService.deleteRefreshToken(account);
	}
}