package com.github.bogdanovmn.authservice.feature.oauth2;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.bogdanovmn.authservice.common.domain.Account;
import com.github.bogdanovmn.authservice.common.domain.AccountService;
import com.github.bogdanovmn.authservice.feature.token.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OAuth2ServiceTest {

	private final AccountService accountService = mock(AccountService.class);
	private final JwtService jwtService = mock(JwtService.class);
	private final StubClient client = new StubClient();
	private final Cache<String, OAuth2State> stateCache = Caffeine.newBuilder().build();

	private OAuth2Service service;

	@BeforeEach
	void setUp() {
		service = new OAuth2Service(
			properties("client-id", "secret", "id"),
			List.of(client),
			stateCache,
			accountService,
			jwtService
		);
	}

	@Test
	void authorizationUrlUsesPkceAndRemembersState() {
		String url = service.authorizationUrl("github", "https://app.example/cb?state=1", null);

		assertEquals("https://provider.example/authorize", url);
		assertNotNull(client.lastRequest.get());
		assertNotNull(client.lastRequest.get().getCodeChallenge());
		assertEquals("client-id", client.lastRequest.get().getConfig().getClientId());
		assertEquals(
			"http://localhost:8091/oauth2/callback/github",
			client.lastRequest.get().getRedirectUri()
		);

		OAuth2State state = stateCache.asMap().get(client.lastRequest.get().getState());
		assertNotNull(state);
		assertEquals("github", state.getProvider());
		assertEquals("https://app.example/cb?state=1", state.getFrom());
		assertEquals("https://app.example/cb?state=1", state.getClientUrl());
		assertNotNull(state.getCodeVerifier());
	}

	@Test
	void authorizationUrlRejectsNonHttpRedirectTarget() {
		service.authorizationUrl("github", "javascript:alert(1)", null);

		OAuth2State state = stateCache.asMap().get(client.lastRequest.get().getState());
		assertNull(state.getFrom());
	}

	@Test
	void loginLinksAccountAndNormalizesEmail() {
		client.tokenResponse = OAuth2TokenResponse.builder()
			.accessToken("access-token")
			.userId("42")
			.build();
		client.userInfo = OAuth2UserInfo.builder()
			.providerUserId("42")
			.email("Joe@Mail.RU")
			.name(" Joe ")
			.build();
		Account account = Account.builder()
			.email("joe@mail.ru")
			.build();
		when(accountService.findOrCreateOAuthAccount("github", "42", "joe@mail.ru", "Joe"))
			.thenReturn(account);
		stateCache.put(
			"state-1",
			OAuth2State.builder()
				.provider("github")
				.from("https://app.example/cb")
				.clientUrl("https://app.example/cb?state=1")
				.codeVerifier("verifier")
				.createdAt(Instant.now())
				.build()
		);

		OAuth2LoginResult result = service.login("github", "provider-code", "state-1", null, null, null);

		assertEquals(account, result.getAccount());
		assertEquals("https://app.example/cb", result.getFrom());
		assertEquals("https://app.example/cb?state=1", result.getClientUrl());
		verify(jwtService).ensureAccountIsAvailable(account);
		verify(accountService).findOrCreateOAuthAccount("github", "42", "joe@mail.ru", "Joe");
	}

	@Test
	void loginUsesEmailReturnedWithTokenWhenUserInfoHasNone() {
		client.tokenResponse = OAuth2TokenResponse.builder()
			.accessToken("access-token")
			.userId("42")
			.email("USER@Example.COM")
			.build();
		client.userInfo = OAuth2UserInfo.builder()
			.providerUserId("42")
			.build();
		Account account = Account.builder()
			.email("user@example.com")
			.build();
		when(accountService.findOrCreateOAuthAccount(eq("github"), eq("42"), eq("user@example.com"), any()))
			.thenReturn(account);
		stateCache.put(
			"state-1",
			OAuth2State.builder()
				.provider("github")
				.codeVerifier("verifier")
				.createdAt(Instant.now())
				.build()
		);

		OAuth2LoginResult result = service.login("github", "provider-code", "state-1", null, null, null);

		assertEquals(account, result.getAccount());
		assertNull(result.getFrom());
		verify(accountService).findOrCreateOAuthAccount(
			"github", "42", "user@example.com", "user"
		);
	}

	@Test
	void loginRequiresAnEmail() {
		client.tokenResponse = OAuth2TokenResponse.builder()
			.accessToken("access-token")
			.userId("42")
			.build();
		client.userInfo = OAuth2UserInfo.builder()
			.providerUserId("42")
			.build();
		stateCache.put(
			"state-1",
			OAuth2State.builder()
				.provider("github")
				.codeVerifier("verifier")
				.createdAt(Instant.now())
				.build()
		);

		OAuth2LoginException ex = assertThrows(
			OAuth2LoginException.class,
			() -> service.login("github", "provider-code", "state-1", null, null, null)
		);
		assertEquals(OAuth2LoginException.EMAIL_REQUIRED, ex.getErrorCode());
	}

	@Test
	void loginRejectsUnknownState() {
		OAuth2LoginException ex = assertThrows(
			OAuth2LoginException.class,
			() -> service.login("github", "provider-code", "missing", null, null, null)
		);
		assertEquals(OAuth2LoginException.INVALID_STATE, ex.getErrorCode());
	}

	@Test
	void stateIsSingleUse() {
		client.tokenResponse = OAuth2TokenResponse.builder()
			.accessToken("access-token")
			.userId("42")
			.email("joe@mail.ru")
			.build();
		client.userInfo = OAuth2UserInfo.builder()
			.providerUserId("42")
			.email("joe@mail.ru")
			.name("Joe")
			.build();
		when(accountService.findOrCreateOAuthAccount(any(), any(), any(), any()))
			.thenReturn(Account.builder().build());
		stateCache.put(
			"state-1",
			OAuth2State.builder()
				.provider("github")
				.codeVerifier("verifier")
				.createdAt(Instant.now())
				.build()
		);

		service.login("github", "provider-code", "state-1", null, null, null);

		OAuth2LoginException ex = assertThrows(
			OAuth2LoginException.class,
			() -> service.login("github", "provider-code", "state-1", null, null, null)
		);
		assertEquals(OAuth2LoginException.INVALID_STATE, ex.getErrorCode());
	}

	@Test
	void loginRejectsStateOfAnotherProvider() {
		stateCache.put(
			"state-1",
			OAuth2State.builder()
				.provider("vk")
				.codeVerifier("verifier")
				.createdAt(Instant.now())
				.build()
		);

		OAuth2LoginException ex = assertThrows(
			OAuth2LoginException.class,
			() -> service.login("github", "provider-code", "state-1", null, null, null)
		);
		assertEquals(OAuth2LoginException.INVALID_STATE, ex.getErrorCode());
	}

	@Test
	void unknownProviderIsRejected() {
		OAuth2LoginException ex = assertThrows(
			OAuth2LoginException.class,
			() -> service.authorizationUrl("vk", null, null)
		);
		assertEquals(OAuth2LoginException.UNKNOWN_PROVIDER, ex.getErrorCode());
	}

	private static OAuth2Properties properties(String clientId, String secret, String mode) {
		OAuth2Properties.Provider provider = new OAuth2Properties.Provider()
			.setClientId(clientId)
			.setClientSecret(secret)
			.setMode(mode);
		return new OAuth2Properties()
			.setRedirectBaseUrl("http://localhost:8091")
			.setFrontendUrl("http://localhost:5173/sso/")
			.setProviders(java.util.Map.of("github", provider));
	}

	private static class StubClient implements OAuth2ProviderClient {
		private final AtomicReference<OAuth2AuthorizationRequest> lastRequest = new AtomicReference<>();
		private OAuth2TokenResponse tokenResponse;
		private OAuth2UserInfo userInfo;

		@Override
		public String getName() {
			return "github";
		}

		@Override
		public boolean supportsPkce(OAuth2Properties.Provider config) {
			return true;
		}

		@Override
		public String buildAuthorizationUrl(OAuth2AuthorizationRequest request) {
			lastRequest.set(request);
			return "https://provider.example/authorize";
		}

		@Override
		public OAuth2TokenResponse exchangeCode(OAuth2AuthorizationRequest request,
												String code,
												String codeVerifier,
												String deviceId)
		{
			return tokenResponse;
		}

		@Override
		public OAuth2UserInfo fetchUserInfo(OAuth2AuthorizationRequest request, String accessToken) {
			return userInfo;
		}
	}
}
