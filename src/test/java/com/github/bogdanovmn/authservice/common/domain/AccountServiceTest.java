package com.github.bogdanovmn.authservice.common.domain;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AccountServiceTest {

	private final AccountRepository accountRepository = mock(AccountRepository.class);
	private final AccountOAuthIdentityRepository identityRepository =
		mock(AccountOAuthIdentityRepository.class);
	private final RoleRepository roleRepository = mock(RoleRepository.class);

	private final AccountService service = new AccountService(
		accountRepository,
		identityRepository,
		roleRepository
	);

	@Test
	void knownIdentityIsReusedAndRefreshed() {
		Account account = new Account().setEmail("joe@mail.ru");
		AccountOAuthIdentity identity = new AccountOAuthIdentity()
			.setAccount(account)
			.setProvider("github")
			.setProviderUserId("42")
			.setEmail("old@mail.ru")
			.setName("Old Name");
		when(identityRepository.findByProviderAndProviderUserId("github", "42"))
			.thenReturn(Optional.of(identity));

		Account result = service.findOrCreateOAuthAccount(
			"github", "42", "new@mail.ru", "New Name"
		);

		assertSame(account, result);
		assertEquals("new@mail.ru", identity.getEmail());
		assertEquals("New Name", identity.getName());
		verify(identityRepository, never()).save(any());
		verify(accountRepository, never()).findByEmailIgnoreCase(anyString());
	}

	@Test
	void knownEmailIsLinkedToTheExistingAccount() {
		Account account = new Account().setEmail("joe@mail.ru");
		when(identityRepository.findByProviderAndProviderUserId("github", "42"))
			.thenReturn(Optional.empty());
		when(accountRepository.findByEmailIgnoreCase("joe@mail.ru"))
			.thenReturn(Optional.of(account));

		Account result = service.findOrCreateOAuthAccount(
			"github", "42", "joe@mail.ru", "Joe"
		);

		assertSame(account, result);
		verify(accountRepository, never()).save(any());

		ArgumentCaptor<AccountOAuthIdentity> captor =
			ArgumentCaptor.forClass(AccountOAuthIdentity.class);
		verify(identityRepository).save(captor.capture());
		assertSame(account, captor.getValue().getAccount());
		assertEquals("github", captor.getValue().getProvider());
		assertEquals("42", captor.getValue().getProviderUserId());
		assertEquals("joe@mail.ru", captor.getValue().getEmail());
	}

	@Test
	void unknownEmailCreatesAnAccountWithTheUserRole() {
		Role userRole = new Role();
		when(identityRepository.findByProviderAndProviderUserId("github", "42"))
			.thenReturn(Optional.empty());
		when(accountRepository.findByEmailIgnoreCase("joe@mail.ru"))
			.thenReturn(Optional.empty());
		when(roleRepository.getBasicUserRole()).thenReturn(userRole);
		when(accountRepository.save(any(Account.class)))
			.thenAnswer(invocation -> invocation.getArgument(0));

		Account result = service.findOrCreateOAuthAccount(
			"github", "42", "joe@mail.ru", "Joe"
		);

		assertEquals("joe@mail.ru", result.getEmail());
		assertEquals("Joe", result.getName());
		assertTrue(result.getEncodedPassword().startsWith("{"));
		assertTrue(result.getRoles().contains(userRole));
		verify(identityRepository).save(any(AccountOAuthIdentity.class));
	}
}
