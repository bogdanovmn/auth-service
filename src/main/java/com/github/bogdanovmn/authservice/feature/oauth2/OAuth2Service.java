package com.github.bogdanovmn.authservice.feature.oauth2;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.bogdanovmn.authservice.common.domain.Account;
import com.github.bogdanovmn.authservice.common.domain.AccountService;
import com.github.bogdanovmn.authservice.feature.token.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
class OAuth2Service {
	private static final SecureRandom RANDOM = new SecureRandom();
	private static final Base64.Encoder BASE64_URL = Base64.getUrlEncoder().withoutPadding();

	private final OAuth2Properties properties;
	private final List<OAuth2ProviderClient> clients;
	private final Cache<String, OAuth2State> oauth2StateCache;
	private final AccountService accountService;
	private final JwtService jwtService;

	String authorizationUrl(String provider, String from) {
		OAuth2ProviderClient client = client(provider);
		OAuth2Properties.Provider config = config(provider);
		if (config.getClientId() == null || config.getClientId().isBlank()) {
			throw new OAuth2LoginException(
				OAuth2LoginException.UNKNOWN_PROVIDER,
				"Provider '%s' has no client id configured".formatted(provider)
			);
		}

		String state = randomToken();
		String codeVerifier = client.supportsPkce(config) ? randomToken() : null;
		String codeChallenge = codeVerifier != null ? codeChallenge(codeVerifier) : null;

		oauth2StateCache.put(
			state,
			OAuth2State.builder()
				.provider(provider)
				.from(safeRedirect(from))
				.codeVerifier(codeVerifier)
				.createdAt(Instant.now())
				.build()
		);

		return client.buildAuthorizationUrl(
			OAuth2AuthorizationRequest.builder()
				.provider(provider)
				.config(config)
				.redirectUri(redirectUri(provider))
				.state(state)
				.codeChallenge(codeChallenge)
				.build()
		);
	}

	OAuth2LoginResult login(String provider,
							String code,
							String state,
							String deviceId,
							String error,
							String errorDescription)
	{
		if (error != null && !error.isBlank()) {
			throw new OAuth2LoginException(
				OAuth2LoginException.ACCESS_DENIED.equals(error)
					? OAuth2LoginException.ACCESS_DENIED
					: OAuth2LoginException.INVALID_REQUEST,
				errorDescription != null && !errorDescription.isBlank() ? errorDescription : error
			);
		}
		if (state == null || state.isBlank()) {
			throw new OAuth2LoginException(
				OAuth2LoginException.INVALID_STATE,
				"The OAuth2 state parameter is missing"
			);
		}

		OAuth2State savedState = oauth2StateCache.asMap().remove(state);
		if (savedState == null || !savedState.getProvider().equals(provider)) {
			throw new OAuth2LoginException(
				OAuth2LoginException.INVALID_STATE,
				"The OAuth2 state is unknown or has expired"
			);
		}
		if (code == null || code.isBlank()) {
			throw new OAuth2LoginException(
				OAuth2LoginException.INVALID_REQUEST,
				"The OAuth2 code parameter is missing"
			);
		}

		OAuth2ProviderClient client = client(provider);
		OAuth2Properties.Provider config = config(provider);
		OAuth2AuthorizationRequest request = OAuth2AuthorizationRequest.builder()
			.provider(provider)
			.config(config)
			.redirectUri(redirectUri(provider))
			.state(state)
			.build();

		OAuth2TokenResponse token = client.exchangeCode(
			request,
			code,
			savedState.getCodeVerifier(),
			deviceId
		);
		OAuth2UserInfo userInfo = client.fetchUserInfo(request, token.getAccessToken());

		String email = normalizeEmail(
			userInfo.getEmail() != null ? userInfo.getEmail() : token.getEmail()
		);
		if (email == null) {
			throw new OAuth2LoginException(
				OAuth2LoginException.EMAIL_REQUIRED,
				"Provider '%s' did not return an email address".formatted(provider)
			);
		}

		String providerUserId = userInfo.getProviderUserId() != null
			? userInfo.getProviderUserId()
			: token.getUserId();
		if (providerUserId == null || providerUserId.isBlank()) {
			throw new OAuth2LoginException(
				OAuth2LoginException.PROVIDER_ERROR,
				"Provider '%s' did not return a user id".formatted(provider)
			);
		}

		Account account = accountService.findOrCreateOAuthAccount(
			provider,
			providerUserId,
			email,
			nameOrFallback(userInfo.getName(), email)
		);
		jwtService.ensureAccountIsAvailable(account);

		return OAuth2LoginResult.builder()
			.account(account)
			.from(savedState.getFrom())
			.build();
	}

	private OAuth2ProviderClient client(String provider) {
		return clients.stream()
			.filter(candidate -> candidate.getName().equals(provider))
			.findFirst()
			.orElseThrow(
				() -> new OAuth2LoginException(
					OAuth2LoginException.UNKNOWN_PROVIDER,
					"Unknown OAuth2 provider '%s'".formatted(provider)
				)
			);
	}

	private OAuth2Properties.Provider config(String provider) {
		return Optional.ofNullable(properties.getProviders())
			.map(providers -> providers.get(provider))
			.orElseThrow(
				() -> new OAuth2LoginException(
					OAuth2LoginException.UNKNOWN_PROVIDER,
					"Provider '%s' is not configured".formatted(provider)
				)
			);
	}

	private String redirectUri(String provider) {
		String base = properties.getRedirectBaseUrl();
		if (base.endsWith("/")) {
			base = base.substring(0, base.length() - 1);
		}
		return "%s/oauth2/callback/%s".formatted(base, provider);
	}

	private static String safeRedirect(String from) {
		if (from == null || from.isBlank() || !isHttpUrl(from)) {
			return null;
		}
		return from;
	}

	private static boolean isHttpUrl(String value) {
		try {
			URI uri = URI.create(value);
			String scheme = uri.getScheme();
			return uri.isAbsolute()
				&& ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme));
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

	private static String normalizeEmail(String email) {
		if (email == null || email.isBlank()) {
			return null;
		}
		return email.trim().toLowerCase(Locale.ROOT);
	}

	private static String nameOrFallback(String name, String email) {
		if (name != null && !name.isBlank()) {
			return name.trim();
		}
		int at = email.indexOf('@');
		return at > 0 ? email.substring(0, at) : email;
	}

	private static String randomToken() {
		byte[] bytes = new byte[32];
		RANDOM.nextBytes(bytes);
		return BASE64_URL.encodeToString(bytes);
	}

	private static String codeChallenge(String codeVerifier) {
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256")
				.digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));
			return BASE64_URL.encodeToString(digest);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-256 is not available", e);
		}
	}
}
