package com.github.bogdanovmn.authservice.feature.oauth2.yandex;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bogdanovmn.authservice.feature.oauth2.OAuth2AuthorizationRequest;
import com.github.bogdanovmn.authservice.feature.oauth2.OAuth2Properties;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class YandexClientTest {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	void authorizationUrlContainsPkceAndScope() {
		YandexClient client = new YandexClient(objectMapper, null);
		OAuth2Properties.Provider config = new OAuth2Properties.Provider()
			.setClientId("yandex-client")
			.setClientSecret("secret")
			.setScope("login:email,login:info");

		assertTrue(client.supportsPkce(config));

		String url = client.buildAuthorizationUrl(request("yandex", config));

		assertTrue(url.startsWith("https://oauth.yandex.ru/authorize?"));
		Map<String, String> params = query(url);
		assertEquals("yandex-client", params.get("client_id"));
		assertEquals("code", params.get("response_type"));
		assertEquals("state-1", params.get("state"));
		assertEquals("login:email,login:info", params.get("scope"));
		assertEquals("challenge-1", params.get("code_challenge"));
		assertEquals("S256", params.get("code_challenge_method"));
		assertEquals(
			"http://localhost:8091/oauth2/callback/yandex",
			params.get("redirect_uri")
		);
	}

	private static OAuth2AuthorizationRequest request(String provider,
													  OAuth2Properties.Provider config)
	{
		return OAuth2AuthorizationRequest.builder()
			.provider(provider)
			.config(config)
			.redirectUri("http://localhost:8091/oauth2/callback/" + provider)
			.state("state-1")
			.codeChallenge("challenge-1")
			.build();
	}

	private static Map<String, String> query(String url) {
		Map<String, String> result = new HashMap<>();
		String rawQuery = URI.create(url).getRawQuery();
		for (String pair : rawQuery.split("&")) {
			String[] parts = pair.split("=", 2);
			result.put(
				URLDecoder.decode(parts[0], StandardCharsets.UTF_8),
				parts.length > 1
					? URLDecoder.decode(parts[1], StandardCharsets.UTF_8)
					: ""
			);
		}
		return result;
	}
}
