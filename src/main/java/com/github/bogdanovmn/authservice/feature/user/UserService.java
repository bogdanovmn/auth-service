package com.github.bogdanovmn.authservice.feature.user;

import com.github.bogdanovmn.authservice.common.domain.Account;
import com.github.bogdanovmn.authservice.common.domain.AccountRepository;
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

	@Transactional(readOnly = true)
	public List<UserResponse> all() {
		return accountRepository.findAll(Sort.by("email")).stream()
			.map(
				account -> UserResponse.builder()
					.id(account.getId())
					.name(account.getName())
					.email(account.getEmail())
					.status(account.getStatus().name())
					.createdAt(account.getCreatedAt())
					.updatedAt(account.getUpdatedAt())
					.build()
			)
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
}