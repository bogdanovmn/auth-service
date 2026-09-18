package com.github.bogdanovmn.authservice.feature.activity;

import com.github.bogdanovmn.authservice.common.domain.Account;
import com.github.bogdanovmn.authservice.common.domain.AccountRepository;
import com.github.bogdanovmn.authservice.common.domain.AccountSecurityEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
class AccountActivityService {
	private final AccountRepository accountRepository;
	private final AccountSecurityEventRepository accountSecurityEventRepository;

	@Transactional(readOnly = true)
	public UserActivityResponse events(UUID accountId) {
		Account account = accountRepository.findById(accountId)
			.orElseThrow(
				() -> new NoSuchElementException("User with id '%s' has not been found".formatted(accountId))
			);

		List<UserActivityResponse.Event> events = accountSecurityEventRepository
			.findTop100ByAccount_IdOrderByCreatedAtDesc(accountId)
			.stream()
			.map(
				event -> UserActivityResponse.Event.builder()
					.id(event.getId())
					.type(event.getType().name())
					.ip(event.getIp())
					.userAgent(event.getUserAgent())
					.createdAt(event.getCreatedAt())
					.build()
			)
			.toList();

		return UserActivityResponse.builder()
			.account(
				UserActivityResponse.AccountInfo.builder()
					.id(account.getId())
					.name(account.getName())
					.email(account.getEmail())
					.build()
			)
			.events(events)
			.build();
	}
}