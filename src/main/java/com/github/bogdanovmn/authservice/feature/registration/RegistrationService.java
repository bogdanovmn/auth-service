package com.github.bogdanovmn.authservice.feature.registration;

import com.github.bogdanovmn.authservice.common.domain.Account;
import com.github.bogdanovmn.authservice.common.domain.AccountRepository;
import com.github.bogdanovmn.authservice.common.domain.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
class RegistrationService {
	private final AccountRepository accountRepository;
	private final RoleRepository roleRepository;

	@Transactional
	public void createAccount(RegistrationRequest request) {
		Set<Role> roles = new HashSet<>();
		roles.add(roleRepository.getBasicUserRole());

		if (accountRepository.count() == 0) {
			roles.add(
				roleRepository.getBasicAdminRole()
			);
		}
		accountRepository.save(
			new Account()
				.setEmail(request.getEmail())
				.setName(request.getAccountName())
				.setEncodedPassword(
					PasswordEncoderFactories.createDelegatingPasswordEncoder().encode(
						request.getPassword()
					)
				)
				.setRoles(roles)
		);
	}
}
