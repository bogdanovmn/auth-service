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

	public Account getByName(String name) {
		return accountRepository.findByName(name).orElseThrow(
			() -> new NoSuchElementException("User with name '%s' has not been found".formatted(name))
		);
	}

	public Optional<Account> getByEmail(String email) {
		return accountRepository.findByEmail(email);
	}

}
