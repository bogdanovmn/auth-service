package com.github.bogdanovmn.authservice.feature.oauth2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.net.http.HttpClient;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;

@Component
class VkClient extends AbstractOAuth2Client {
	static final String NAME = "vk";
	static final String MODE_ID = "id";
	static final String MODE_LEGACY = "legacy";

	private static final String AUTHORIZE_URL = "https://id.vk.ru/authorize";
	private static final String TOKEN_URL = "https://id.vk.ru/oauth2/auth";
	private static final String USER_INFO_URL = "https://id.vk.ru/oauth2/user_info";

	private static final String LEGACY_AUTHORIZE_URL = "https://oauth.vk.com/authorize";
	private static final String LEGACY_TOKEN_URL = "https://oauth.vk.com/access_token";
	private static final String LEGACY_USER_INFO_URL = "https://api.vk.com/method/users.get";

	private static final String DEFAULT_SCOPE = "email";

	VkClient(ObjectMapper objectMapper, HttpClient httpClient) {
		super(objectMapper, httpClient);
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public boolean supportsPkce(OAuth2Properties.Provider config) {
		return !isLegacy(config);
	}

	@Override
	public String buildAuthorizationUrl(OAuth2AuthorizationRequest request) {
		boolean legacy = isLegacy(request.getConfig());

		Map<String, String> params = new LinkedHashMap<>();
		params.put("client_id", request.getConfig().getClientId());
		params.put("redirect_uri", request.getRedirectUri());
		params.put("response_type", "code");
		params.put("scope", scope(request.getConfig()));
		params.put("state", request.getState());
		if (legacy) {
			params.put("v", apiVersion(request.getConfig()));
		} else if (request.getCodeChallenge() != null) {
			params.put("code_challenge", request.getCodeChallenge());
			params.put("code_challenge_method", "S256");
		}

		return buildUrl(
			legacy
				? url(request.getConfig().getAuthorizeUrl(), LEGACY_AUTHORIZE_URL)
				: url(request.getConfig().getAuthorizeUrl(), AUTHORIZE_URL),
			params
		);
	}

	@Override
	public OAuth2TokenResponse exchangeCode(OAuth2AuthorizationRequest request,
											String code,
											String codeVerifier,
											String deviceId)
	{
		return isLegacy(request.getConfig())
			? exchangeLegacyCode(request, code)
			: exchangeIdCode(request, code, codeVerifier, deviceId);
	}

	private OAuth2TokenResponse exchangeIdCode(OAuth2AuthorizationRequest request,
											   String code,
											   String codeVerifier,
											   String deviceId)
	{
		Map<String, String> params = new LinkedHashMap<>();
		params.put("grant_type", "authorization_code");
		params.put("code_verifier", codeVerifier);
		params.put("redirect_uri", request.getRedirectUri());
		params.put("code", code);
		params.put("client_id", request.getConfig().getClientId());
		params.put("device_id", deviceId);
		params.put("state", request.getState());

		JsonNode response = postForm(
			url(request.getConfig().getTokenUrl(), TOKEN_URL),
			params,
			null
		);
		return OAuth2TokenResponse.builder()
			.accessToken(text(response, "access_token"))
			.userId(text(response, "user_id"))
			.build();
	}

	private OAuth2TokenResponse exchangeLegacyCode(OAuth2AuthorizationRequest request, String code) {
		Map<String, String> params = new LinkedHashMap<>();
		params.put("client_id", request.getConfig().getClientId());
		params.put("client_secret", request.getConfig().getClientSecret());
		params.put("redirect_uri", request.getRedirectUri());
		params.put("code", code);
		params.put("v", apiVersion(request.getConfig()));

		JsonNode response = postForm(
			url(request.getConfig().getTokenUrl(), LEGACY_TOKEN_URL),
			params,
			null
		);
		return OAuth2TokenResponse.builder()
			.accessToken(text(response, "access_token"))
			.userId(text(response, "user_id"))
			.email(blankToNull(text(response, "email")))
			.build();
	}

	@Override
	public OAuth2UserInfo fetchUserInfo(OAuth2AuthorizationRequest request, String accessToken) {
		return isLegacy(request.getConfig())
			? fetchLegacyUserInfo(request, accessToken)
			: fetchIdUserInfo(request, accessToken);
	}

	private OAuth2UserInfo fetchIdUserInfo(OAuth2AuthorizationRequest request, String accessToken) {
		Map<String, String> params = new LinkedHashMap<>();
		params.put("client_id", request.getConfig().getClientId());
		params.put("access_token", accessToken);

		JsonNode response = postForm(
			url(request.getConfig().getUserInfoUrl(), USER_INFO_URL),
			params,
			null
		);
		JsonNode user = response.path("user");
		return OAuth2UserInfo.builder()
			.providerUserId(text(user, "user_id"))
			.email(blankToNull(text(user, "email")))
			.name(fullName(text(user, "first_name"), text(user, "last_name")))
			.build();
	}

	private OAuth2UserInfo fetchLegacyUserInfo(OAuth2AuthorizationRequest request, String accessToken) {
		Map<String, String> params = new LinkedHashMap<>();
		params.put("access_token", accessToken);
		params.put("v", apiVersion(request.getConfig()));

		// The legacy API takes the token as a query parameter.
		JsonNode response = getJson(
			buildUrl(
				url(request.getConfig().getUserInfoUrl(), LEGACY_USER_INFO_URL),
				params
			),
			null
		);
		JsonNode user = response.path("response").path(0);
		return OAuth2UserInfo.builder()
			.providerUserId(text(user, "id"))
			.name(fullName(text(user, "first_name"), text(user, "last_name")))
			.build();
	}

	private boolean isLegacy(OAuth2Properties.Provider config) {
		return config != null && MODE_LEGACY.equalsIgnoreCase(config.getMode());
	}

	private String scope(OAuth2Properties.Provider config) {
		String configured = blankToNull(config.getScope());
		return configured != null ? configured : DEFAULT_SCOPE;
	}

	private String apiVersion(OAuth2Properties.Provider config) {
		String configured = blankToNull(config.getApiVersion());
		return configured != null ? configured : "5.199";
	}

	private String url(String configured, String fallback) {
		String value = blankToNull(configured);
		return value != null ? value : fallback;
	}

	private static String fullName(String firstName, String lastName) {
		StringJoiner joiner = new StringJoiner(" ");
		if (blankToNull(firstName) != null) {
			joiner.add(firstName.trim());
		}
		if (blankToNull(lastName) != null) {
			joiner.add(lastName.trim());
		}
		String name = joiner.toString();
		return name.isBlank() ? null : name;
	}
}
