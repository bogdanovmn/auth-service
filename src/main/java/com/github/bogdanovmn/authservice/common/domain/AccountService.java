package com.github.bogdanovmn.authservice.common.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService {
	private static final PasswordEncoder PASSWORD_ENCODER =
		PasswordEncoderFactories.createDelegatingPasswordEncoder();

	private final AccountRepository accountRepository;
	private final AccountOAuthIdentityRepository accountOAuthIdentityRepository;
	private final RoleRepository roleRepository;

	public Account getByEmail(String email) {
		return accountRepository.findByEmail(email).orElseThrow(
			() -> new NoSuchElementException("User with email '%s' has not been found".formatted(email))
		);
	}

	public Account getById(UUID id) {
		return accountRepository.findById(id).orElseThrow(
			() -> new NoSuchElementException("User with id '%s' has not been found".formatted(id))
		);
	}

	public Optional<Account> findByEmail(String email) {
		return accountRepository.findByEmail(email);
	}

	@Transactional
	public Account findOrCreateOAuthAccount(String provider, String providerUserId, String email, String name) {
		Optional<AccountOAuthIdentity> existingIdentity =
			accountOAuthIdentityRepository.findByProviderAndProviderUserId(provider, providerUserId);

		if (existingIdentity.isPresent()) {
			AccountOAuthIdentity identity = existingIdentity.get();
			Account account = getById(identity.getAccountId());
			updateIdentity(identity, email, name);
			return account;
		}

		Account account = accountRepository.findByEmailIgnoreCase(email)
			.orElseGet(() -> createAccount(email, name));

		accountOAuthIdentityRepository.create(
			AccountOAuthIdentity.builder()
				.accountId(account.getId())
				.provider(provider)
				.providerUserId(providerUserId)
				.email(email)
				.name(name)
				.build()
		);
		return account;
	}

	private Account createAccount(String email, String name) {
		Set<Role> roles = new HashSet<>();
		roles.add(roleRepository.getBasicUserRole());

		return accountRepository.create(
			Account.builder()
				.email(email)
				.name(name)
				.encodedPassword(
					PASSWORD_ENCODER.encode(UUID.randomUUID().toString())
				)
				.roles(roles)
				.build()
		);
	}

	private void updateIdentity(AccountOAuthIdentity identity, String email, String name) {
		AccountOAuthIdentity updated = identity.toBuilder()
			.email(email != null ? email : identity.getEmail())
			.name(name != null ? name : identity.getName())
			.build();
		if (!updated.equals(identity)) {
			accountOAuthIdentityRepository.update(updated);
		}
	}
}
