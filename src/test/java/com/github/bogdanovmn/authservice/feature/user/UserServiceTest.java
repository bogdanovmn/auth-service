package com.github.bogdanovmn.authservice.feature.user;

import com.github.bogdanovmn.authservice.common.domain.Account;
import com.github.bogdanovmn.authservice.common.domain.AccountRepository;
import com.github.bogdanovmn.authservice.common.domain.AccountSecurityEventType;
import com.github.bogdanovmn.authservice.feature.token.JwtService;
import com.github.bogdanovmn.authservice.infrastructure.audit.SecurityEventLogger;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceTest {
	private static final String ADMIN_EMAIL = "admin@mail.ru";
	private static final String USER_EMAIL = "joe@mail.ru";

	private final AccountRepository accountRepository = mock(AccountRepository.class);
	private final PasswordResetLinkService passwordResetLinkService = mock(PasswordResetLinkService.class);
	private final JwtService jwtService = mock(JwtService.class);
	private final SecurityEventLogger securityEventLogger = mock(SecurityEventLogger.class);
	private final UserService userService = new UserService(
		accountRepository, passwordResetLinkService, jwtService, securityEventLogger
	);

	@Test
	void unknownAccountIsReportedAsNotFound() {
		final UUID accountId = UUID.randomUUID();
		when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

		assertThrows(
			NoSuchElementException.class,
			() -> userService.changeStatus(accountId, Account.Status.INACTIVE, ADMIN_EMAIL)
		);
	}

	@Test
	void ownStatusCannotBeChanged() {
		final UUID accountId = UUID.randomUUID();
		when(accountRepository.findById(accountId))
			.thenReturn(Optional.of(new Account().setId(accountId).setEmail(ADMIN_EMAIL)));

		assertThrows(
			IllegalArgumentException.class,
			() -> userService.changeStatus(accountId, Account.Status.INACTIVE, ADMIN_EMAIL)
		);
	}

	@Test
	void deactivationRevokesRefreshToken() {
		final UUID accountId = UUID.randomUUID();
		Account account = new Account()
			.setId(accountId)
			.setEmail(USER_EMAIL)
			.setStatus(Account.Status.ACTIVE);

		when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

		UserResponse response = userService.changeStatus(accountId, Account.Status.INACTIVE, ADMIN_EMAIL);

		assertEquals(Account.Status.INACTIVE, account.getStatus());
		assertEquals("INACTIVE", response.getStatus());
		verify(jwtService).deleteRefreshToken(account);
		verify(securityEventLogger).log(accountId, AccountSecurityEventType.STATUS_CHANGED);
	}

	@Test
	void activationKeepsRefreshToken() {
		final UUID accountId = UUID.randomUUID();
		Account account = new Account()
			.setId(accountId)
			.setEmail(USER_EMAIL)
			.setStatus(Account.Status.INACTIVE);

		when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

		UserResponse response = userService.changeStatus(accountId, Account.Status.ACTIVE, ADMIN_EMAIL);

		assertEquals(Account.Status.ACTIVE, account.getStatus());
		assertEquals("ACTIVE", response.getStatus());
		verify(jwtService, never()).deleteRefreshToken(any());
	}

	@Test
	void sameStatusChangesNothing() {
		final UUID accountId = UUID.randomUUID();
		Account account = new Account()
			.setId(accountId)
			.setEmail(USER_EMAIL)
			.setStatus(Account.Status.ACTIVE);

		when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

		userService.changeStatus(accountId, Account.Status.ACTIVE, ADMIN_EMAIL);

		verify(jwtService, never()).deleteRefreshToken(any());
		verify(securityEventLogger, never()).log(any(), any());
	}
}
