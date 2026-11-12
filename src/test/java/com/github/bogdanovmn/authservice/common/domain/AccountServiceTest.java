package com.github.bogdanovmn.authservice.common.domain;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;
import java.util.UUID;

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
		UUID accountId = UUID.randomUUID();
		Account account = Account.builder()
			.id(accountId)
			.email("joe@mail.ru")
			.build();
		AccountOAuthIdentity identity = AccountOAuthIdentity.builder()
			.id(UUID.randomUUID())
			.accountId(accountId)
			.provider("github")
			.providerUserId("42")
			.email("old@mail.ru")
			.name("Old Name")
			.build();
		when(identityRepository.findByProviderAndProviderUserId("github", "42"))
			.thenReturn(Optional.of(identity));
		when(accountRepository.findById(accountId))
			.thenReturn(Optional.of(account));

		Account result = service.findOrCreateOAuthAccount(
			"github", "42", "new@mail.ru", "New Name"
		);

		assertSame(account, result);

		ArgumentCaptor<AccountOAuthIdentity> captor =
			ArgumentCaptor.forClass(AccountOAuthIdentity.class);
		verify(identityRepository).update(captor.capture());
		assertEquals("new@mail.ru", captor.getValue().getEmail());
		assertEquals("New Name", captor.getValue().getName());
		verify(identityRepository, never()).create(any());
		verify(accountRepository, never()).findByEmailIgnoreCase(anyString());
	}

	@Test
	void knownEmailIsLinkedToTheExistingAccount() {
		UUID accountId = UUID.randomUUID();
		Account account = Account.builder()
			.id(accountId)
			.email("joe@mail.ru")
			.build();
		when(identityRepository.findByProviderAndProviderUserId("github", "42"))
			.thenReturn(Optional.empty());
		when(accountRepository.findByEmailIgnoreCase("joe@mail.ru"))
			.thenReturn(Optional.of(account));

		Account result = service.findOrCreateOAuthAccount(
			"github", "42", "joe@mail.ru", "Joe"
		);

		assertSame(account, result);
		verify(accountRepository, never()).create(any());

		ArgumentCaptor<AccountOAuthIdentity> captor =
			ArgumentCaptor.forClass(AccountOAuthIdentity.class);
		verify(identityRepository).create(captor.capture());
		assertEquals(accountId, captor.getValue().getAccountId());
		assertEquals("github", captor.getValue().getProvider());
		assertEquals("42", captor.getValue().getProviderUserId());
		assertEquals("joe@mail.ru", captor.getValue().getEmail());
	}

	@Test
	void unknownEmailCreatesAnAccountWithTheUserRole() {
		Role userRole = Role.builder()
			.name(Role.Name.user)
			.build();
		when(identityRepository.findByProviderAndProviderUserId("github", "42"))
			.thenReturn(Optional.empty());
		when(accountRepository.findByEmailIgnoreCase("joe@mail.ru"))
			.thenReturn(Optional.empty());
		when(roleRepository.getBasicUserRole()).thenReturn(userRole);
		when(accountRepository.create(any(Account.class)))
			.thenAnswer(invocation -> invocation.getArgument(0));

		Account result = service.findOrCreateOAuthAccount(
			"github", "42", "joe@mail.ru", "Joe"
		);

		assertEquals("joe@mail.ru", result.getEmail());
		assertEquals("Joe", result.getName());
		assertTrue(result.getEncodedPassword().startsWith("{"));
		assertTrue(result.getRoles().contains(userRole));
		verify(identityRepository).create(any(AccountOAuthIdentity.class));
	}
}
