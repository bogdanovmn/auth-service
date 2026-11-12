package com.github.bogdanovmn.authservice.feature.oauth2.github;

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
class GitHubClient extends AbstractOAuth2Client {
	static final String NAME = "github";

	private static final String AUTHORIZE_URL = "https://github.com/login/oauth/authorize";
	private static final String TOKEN_URL = "https://github.com/login/oauth/access_token";
	private static final String USER_INFO_URL = "https://api.github.com/user";
	private static final String EMAILS_URL = "https://api.github.com/user/emails";

	GitHubClient(ObjectMapper objectMapper, HttpClient httpClient) {
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
		return buildUrl(url(request.getConfig().getAuthorizeUrl(), AUTHORIZE_URL), params);
	}

	@Override
	public OAuth2TokenResponse exchangeCode(OAuth2AuthorizationRequest request,
											String code,
											String codeVerifier,
											String deviceId)
	{
		Map<String, String> params = new LinkedHashMap<>();
		params.put("client_id", request.getConfig().getClientId());
		params.put("client_secret", request.getConfig().getClientSecret());
		params.put("code", code);
		params.put("redirect_uri", request.getRedirectUri());
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
		JsonNode user = getJson(
			url(request.getConfig().getUserInfoUrl(), USER_INFO_URL),
			accessToken
		);

		String email = blankToNull(text(user, "email"));
		if (email == null) {
			email = primaryEmail(accessToken);
		}

		String name = blankToNull(text(user, "name"));
		if (name == null) {
			name = blankToNull(text(user, "login"));
		}

		return OAuth2UserInfo.builder()
			.providerUserId(text(user, "id"))
			.email(email)
			.name(name)
			.build();
	}

	private String primaryEmail(String accessToken) {
		JsonNode emails = getJson(EMAILS_URL, accessToken);
		if (!emails.isArray()) {
			return null;
		}
		String firstVerified = null;
		for (JsonNode node : emails) {
			if (!node.path("verified").asBoolean(false)) {
				continue;
			}
			String email = blankToNull(text(node, "email"));
			if (email == null) {
				continue;
			}
			if (node.path("primary").asBoolean(false)) {
				return email;
			}
			if (firstVerified == null) {
				firstVerified = email;
			}
		}
		return firstVerified;
	}

	private String url(String configured, String fallback) {
		String value = blankToNull(configured);
		return value != null ? value : fallback;
	}
}
