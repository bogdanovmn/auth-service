package com.github.bogdanovmn.authservice.common.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public
class AccountService {
	private final AccountRepository accountRepository;

	/**
	 * Email is the only unique account identifier, so it is used as the principal
	 * name. {@code Account.name} is a display name and must not be looked up.
	 */
	public Account getByEmail(String email) {
		return accountRepository.findByEmail(email).orElseThrow(
			() -> new NoSuchElementException("User with email '%s' has not been found".formatted(email))
		);
	}

	public Optional<Account> findByEmail(String email) {
		return accountRepository.findByEmail(email);
	}
}
