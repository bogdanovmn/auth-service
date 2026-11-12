package com.github.bogdanovmn.authservice.feature.oauth2.yandex;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bogdanovmn.authservice.feature.oauth2.AbstractOAuth2Client;
import com.github.bogdanovmn.authservice.feature.oauth2.OAuth2AuthorizationRequest;
import com.github.bogdanovmn.authservice.feature.oauth2.OAuth2Properties;
import com.github.bogdanovmn.authservice.feature.oauth2.OAuth2TokenResponse;
import com.github.bogdanovmn.authservice.feature.oauth2.OAuth2UserInfo;
import org.springframework.stereotype.Component;

import java.net.http.HttpClient;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
class YandexClient extends AbstractOAuth2Client {
	static final String NAME = "yandex";

	private static final String AUTHORIZE_URL = "https://oauth.yandex.ru/authorize";
	private static final String TOKEN_URL = "https://oauth.yandex.ru/token";
	private static final String USER_INFO_URL = "https://login.yandex.ru/info";

	YandexClient(ObjectMapper objectMapper, HttpClient httpClient) {
		super(objectMapper, httpClient);
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public boolean supportsPkce(OAuth2Properties.Provider config) {
		return true;
	}

	@Override
	public String buildAuthorizationUrl(OAuth2AuthorizationRequest request) {
		Map<String, String> params = new LinkedHashMap<>();
		params.put("client_id", request.getConfig().getClientId());
		params.put("redirect_uri", request.getRedirectUri());
		params.put("response_type", "code");
		params.put("scope", request.getConfig().getScope());
		params.put("state", request.getState());
		if (request.getCodeChallenge() != null) {
			params.put("code_challenge", request.getCodeChallenge());
			params.put("code_challenge_method", "S256");
		}
		return buildUrl(
			url(request.getConfig().getAuthorizeUrl(), AUTHORIZE_URL),
			params
		);
	}

	@Override
	public OAuth2TokenResponse exchangeCode(OAuth2AuthorizationRequest request,
											String code,
											String codeVerifier,
											String deviceId)
	{
		Map<String, String> params = new LinkedHashMap<>();
		params.put("grant_type", "authorization_code");
		params.put("code", code);
		params.put("client_id", request.getConfig().getClientId());
		params.put("client_secret", request.getConfig().getClientSecret());
		params.put("code_verifier", codeVerifier);

		JsonNode response = postForm(
			url(request.getConfig().getTokenUrl(), TOKEN_URL),
			params,
			null
		);
		return OAuth2TokenResponse.builder()
			.accessToken(text(response, "access_token"))
			.build();
	}

	@Override
	public OAuth2UserInfo fetchUserInfo(OAuth2AuthorizationRequest request, String accessToken) {
		JsonNode user = getJsonWithAuthorization(
			buildUrl(
				url(request.getConfig().getUserInfoUrl(), USER_INFO_URL),
				Map.of("format", "json")
			),
			"OAuth " + accessToken
		);

		return OAuth2UserInfo.builder()
			.providerUserId(text(user, "id"))
			.email(email(user))
			.name(firstNonBlank(
				text(user, "display_name"),
				text(user, "real_name"),
				text(user, "login")
			))
			.build();
	}

	private static String email(JsonNode user) {
		String defaultEmail = blankToNull(text(user, "default_email"));
		if (defaultEmail != null) {
			return defaultEmail;
		}
		JsonNode emails = user.path("emails");
		if (emails.isArray() && !emails.isEmpty()) {
			return blankToNull(emails.get(0).asText());
		}
		return null;
	}

	private static String firstNonBlank(String... values) {
		for (String value : values) {
			String normalized = blankToNull(value);
			if (normalized != null) {
				return normalized;
			}
		}
		return null;
	}

	private String url(String configured, String fallback) {
		String value = blankToNull(configured);
		return value != null ? value : fallback;
	}
}
