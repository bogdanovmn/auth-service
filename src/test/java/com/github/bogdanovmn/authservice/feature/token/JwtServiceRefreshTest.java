package com.github.bogdanovmn.authservice.feature.token;

import com.github.bogdanovmn.authservice.common.domain.Account;
import com.github.bogdanovmn.authservice.common.domain.AccountNotAvailableException;
import com.github.bogdanovmn.authservice.common.domain.AccountSecurityEventType;
import com.github.bogdanovmn.authservice.common.domain.AccountService;
import com.github.bogdanovmn.authservice.infrastructure.audit.SecurityEventLogger;
import com.github.bogdanovmn.authservice.infrastructure.config.security.JwtFactory;
import com.github.bogdanovmn.authservice.test.fixture.RoleFixture;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;

import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JwtServiceRefreshTest {
	private static final String EMAIL = "joe@mail.ru";
	private static final String STORED_TOKEN = "presented-refresh-token";

	private final AccountService accountService = mock(AccountService.class);
	private final SecurityEventLogger securityEventLogger = mock(SecurityEventLogger.class);
	private final LoginAttemptLimiter loginAttemptLimiter = mock(LoginAttemptLimiter.class);
	private final RefreshTokenRepository refreshTokenRepository = mock(RefreshTokenRepository.class);
	private final JwtFactory jwtFactory = mock(JwtFactory.class);

	private final JwtService jwtService = new JwtService(
		accountService, jwtFactory, refreshTokenRepository, securityEventLogger, loginAttemptLimiter
	);

	@BeforeEach
	void setUp() {
		when(jwtFactory.createToken(any())).thenReturn("access-token");
		when(jwtFactory.createRefreshToken(any(), any())).thenReturn("new-refresh-token");
		when(refreshTokenRepository.findByAccountId(any())).thenReturn(Optional.empty());
		when(refreshTokenRepository.create(any(), any())).thenReturn(UUID.randomUUID());
	}

	@Test
	void expiredStoredTokenIsRejected() {
		UUID tokenId = UUID.randomUUID();
		stubStoredToken(tokenId, account(), new Date(System.currentTimeMillis() - 1000));

		assertThrows(
			IllegalArgumentException.class,
			() -> jwtService.createTokensByRefreshToken(STORED_TOKEN)
		);

		verify(refreshTokenRepository).deleteById(tokenId);
		verify(refreshTokenRepository, never()).create(any(), any());
	}

	@Test
	void inactiveAccountCannotRefresh() {
		Account account = account().toBuilder()
			.status(Account.Status.INACTIVE)
			.build();
		stubStoredToken(
			UUID.randomUUID(),
			account,
			new Date(System.currentTimeMillis() + 60_000)
		);
		when(accountService.getById(account.getId())).thenReturn(account);

		assertThrows(
			AccountNotAvailableException.class,
			() -> jwtService.createTokensByRefreshToken(STORED_TOKEN)
		);

		verify(refreshTokenRepository, never()).create(any(), any());
	}

	@Test
	void activeAccountGetsANewTokenPair() {
		UUID accountId = UUID.randomUUID();
		Account account = account(accountId);
		stubStoredToken(
			UUID.randomUUID(),
			account,
			new Date(System.currentTimeMillis() + 60_000)
		);
		when(accountService.getById(accountId)).thenReturn(account);

		JwtResponse response = jwtService.createTokensByRefreshToken(STORED_TOKEN);

		assertEquals("access-token", response.getToken());
		assertEquals("new-refresh-token", response.getRefreshToken());
		verify(securityEventLogger).log(accountId, AccountSecurityEventType.REFRESH);
	}

	@Test
	void ssoByEmailIsBlockedForInactiveAccount() {
		when(accountService.getByEmail(EMAIL))
			.thenReturn(account().toBuilder().status(Account.Status.INACTIVE).build());

		assertThrows(
			AccountNotAvailableException.class,
			() -> jwtService.createTokensByAccountEmail(
				EMAIL,
				AccountSecurityEventType.SSO,
				null,
				null
			)
		);

		verify(refreshTokenRepository, never()).create(any(), any());
	}

	@Test
	void logoutIsDoneByEmailNotByName() {
		Account account = account();
		when(accountService.getByEmail(EMAIL)).thenReturn(account);

		jwtService.logout(EMAIL);

		verify(accountService).getByEmail(EMAIL);
		verify(securityEventLogger).log(account.getId(), AccountSecurityEventType.LOGOUT);
	}

	@SuppressWarnings("unchecked")
	private void stubStoredToken(UUID tokenId, Account account, Date expiresAt) {
		Claims claims = mock(Claims.class);
		when(claims.getId()).thenReturn(tokenId.toString());

		Jws<Claims> jws = mock(Jws.class);
		when(jws.getBody()).thenReturn(claims);

		when(jwtFactory.checkSignatureAndReturnClaims(STORED_TOKEN)).thenReturn(jws);
		when(refreshTokenRepository.findById(tokenId))
			.thenReturn(
				Optional.of(
					RefreshToken.builder()
						.id(tokenId)
						.accountId(account.getId())
						.expiresAt(expiresAt)
						.build()
				)
			);
	}

	private static Account account() {
		return account(UUID.randomUUID());
	}

	private static Account account(UUID id) {
		return Account.builder()
			.id(id)
			.name("Joe")
			.email(EMAIL)
			.status(Account.Status.ACTIVE)
			.encodedPassword(PasswordEncoderFactories.createDelegatingPasswordEncoder().encode("secret"))
			.roles(Set.of(RoleFixture.standardUser()))
			.build();
	}
}
