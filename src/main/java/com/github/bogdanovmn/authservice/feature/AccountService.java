package com.github.bogdanovmn.authservice.feature;

import com.github.bogdanovmn.authservice.model.Account;
import com.github.bogdanovmn.authservice.model.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
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

	public Optional<Account> getByEmailAndPassword(String email, String password) {
		return accountRepository.findByEmail(email)
			.filter(
				account -> PasswordEncoderFactories.createDelegatingPasswordEncoder()
					.matches(password, account.getEncodedPassword())
			);
	}

}
