package com.github.bogdanovmn.authservice;

import com.github.bogdanovmn.authservice.model.Account;
import com.github.bogdanovmn.authservice.model.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public
class AccountService {
	private final AccountRepository accountRepository;

	public Optional<Account> getByEmail(String email) {
		return accountRepository.findByEmail(email);
	}

	public Optional<Account> getByEmailAndPassword(String email, String password) {
		return accountRepository.findByEmailAndEncodedPassword(
			email,
			PasswordEncoderFactories.createDelegatingPasswordEncoder()
				.encode(password)
		);
	}

}
