package com.github.bogdanovmn.authservice.feature.user;

import com.github.bogdanovmn.authservice.common.domain.Account;
import com.github.bogdanovmn.authservice.common.domain.AccountRepository;
import com.github.bogdanovmn.authservice.common.domain.AccountSecurityEventType;
import com.github.bogdanovmn.authservice.feature.token.JwtService;
import com.github.bogdanovmn.authservice.infrastructure.audit.SecurityEventLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
class UserService {
	private final AccountRepository accountRepository;
	private final PasswordResetLinkService passwordResetLinkService;
	private final JwtService jwtService;
	private final SecurityEventLogger securityEventLogger;

	@Transactional(readOnly = true)
	public List<UserResponse> all() {
		return accountRepository.findAll(Sort.by("email")).stream()
			.map(UserService::toResponse)
			.toList();
	}

	@Transactional
	public PasswordResetLinkResponse createResetLink(UUID accountId) {
		Account account = accountRepository.findById(accountId)
			.orElseThrow(
				() -> new NoSuchElementException("User with id '%s' has not been found".formatted(accountId))
			);
		PasswordResetToken token = passwordResetLinkService.create(account);
		return PasswordResetLinkResponse.builder()
			.token(token.getId().toString())
			.expiresAt(token.getExpiresAt())
			.ttlInMinutes(passwordResetLinkService.ttlInMinutes())
			.build();
	}

	/**
	 * Deactivating an account must cut its access immediately: the JWT filter
	 * rejects its status on every request, and the refresh token is revoked so
	 * that no new pair of tokens can be obtained.
	 */
	@Transactional
	public UserResponse changeStatus(UUID accountId, Account.Status status, String currentUserEmail) {
		Account account = accountRepository.findById(accountId)
			.orElseThrow(
				() -> new NoSuchElementException("User with id '%s' has not been found".formatted(accountId))
			);

		if (account.getEmail().equalsIgnoreCase(currentUserEmail)) {
			throw new IllegalArgumentException(
				"The status of the account you're logged in with can't be changed"
			);
		}

		if (account.getStatus() != status) {
			account.setStatus(status);
			securityEventLogger.log(account.getId(), AccountSecurityEventType.STATUS_CHANGED);
			if (!status.isAvailable()) {
				jwtService.deleteRefreshToken(account);
			}
		}

		return toResponse(account);
	}

	private static UserResponse toResponse(Account account) {
		return UserResponse.builder()
			.id(account.getId())
			.name(account.getName())
			.email(account.getEmail())
			.status(account.getStatus().name())
			.createdAt(account.getCreatedAt())
			.updatedAt(account.getUpdatedAt())
			.build();
	}
}
