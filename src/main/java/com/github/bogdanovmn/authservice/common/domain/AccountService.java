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

	@Transactional
	public Account findOrCreateOAuthAccount(String provider, String providerUserId, String email, String name) {
		Optional<AccountOAuthIdentity> existingIdentity =
			accountOAuthIdentityRepository.findByProviderAndProviderUserId(provider, providerUserId);

		Account account;
		if (existingIdentity.isPresent()) {
			account = existingIdentity.get().getAccount();
			updateIdentity(existingIdentity.get(), email, name);
		} else {
			account = accountRepository.findByEmailIgnoreCase(email)
				.orElseGet(() -> createAccount(email, name));

			accountOAuthIdentityRepository.save(
				new AccountOAuthIdentity()
					.setAccount(account)
					.setProvider(provider)
					.setProviderUserId(providerUserId)
					.setEmail(email)
					.setName(name)
			);
		}
		return account;
	}

	private Account createAccount(String email, String name) {
		Set<Role> roles = new HashSet<>();
		roles.add(roleRepository.getBasicUserRole());

		return accountRepository.save(
			new Account()
				.setEmail(email)
				.setName(name)
				.setEncodedPassword(
					PASSWORD_ENCODER.encode(UUID.randomUUID().toString())
				)
				.setRoles(roles)
		);
	}

	private void updateIdentity(AccountOAuthIdentity identity, String email, String name) {
		if (email != null && !email.equals(identity.getEmail())) {
			identity.setEmail(email);
		}
		if (name != null && !name.equals(identity.getName())) {
			identity.setName(name);
		}
	}
}
