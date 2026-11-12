package com.github.bogdanovmn.authservice.feature.oauth2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

abstract class AbstractOAuth2Client implements OAuth2ProviderClient {

	protected final ObjectMapper objectMapper;
	protected final HttpClient httpClient;

	protected AbstractOAuth2Client(ObjectMapper objectMapper, HttpClient httpClient) {
		this.objectMapper = objectMapper;
		this.httpClient = httpClient;
	}

	protected JsonNode getJson(String url, String accessToken) {
		HttpRequest.Builder builder = HttpRequest.newBuilder(java.net.URI.create(url))
			.header("Accept", "application/json")
			.header("User-Agent", "auth-service")
			.GET();
		if (accessToken != null) {
			builder.header("Authorization", "Bearer " + accessToken);
		}
		return send(builder.build());
	}

	protected JsonNode postForm(String url, Map<String, String> params, String accessToken) {
		HttpRequest.Builder builder = HttpRequest.newBuilder(java.net.URI.create(url))
			.header("Content-Type", "application/x-www-form-urlencoded")
			.header("Accept", "application/json")
			.header("User-Agent", "auth-service")
			.POST(HttpRequest.BodyPublishers.ofString(form(params)));
		if (accessToken != null) {
			builder.header("Authorization", "Bearer " + accessToken);
		}
		return send(builder.build());
	}

	protected String buildUrl(String baseUrl, Map<String, String> params) {
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl);
		params.forEach(
			(name, value) -> {
				if (value != null) {
					builder.queryParam(name, value);
				}
			}
		);
		return builder.build().encode().toUriString();
	}

	protected String form(Map<String, String> params) {
		return params.entrySet().stream()
			.filter(e -> e.getValue() != null)
			.map(e -> URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8)
				+ "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
			.collect(Collectors.joining("&"));
	}

	protected JsonNode send(HttpRequest request) {
		try {
			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() < 200 || response.statusCode() >= 300) {
				throw providerError(response.body());
			}
			return objectMapper.readTree(response.body());
		} catch (IOException e) {
			throw new OAuth2LoginException(
				OAuth2LoginException.PROVIDER_ERROR,
				"Provider '%s' is not available".formatted(getName())
			);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new OAuth2LoginException(
				OAuth2LoginException.PROVIDER_ERROR,
				"Provider '%s' request has been interrupted".formatted(getName())
			);
		}
	}

	protected OAuth2LoginException providerError(String body) {
		String message = "Provider '%s' returned an error".formatted(getName());
		try {
			JsonNode node = objectMapper.readTree(body);
			if (node.hasNonNull("error_description")) {
				message = node.get("error_description").asText();
			} else if (node.hasNonNull("error_message")) {
				message = node.get("error_message").asText();
			} else if (node.hasNonNull("error")) {
				JsonNode error = node.get("error");
				if (error.isTextual()) {
					message = error.asText();
				} else if (error.hasNonNull("message")) {
					message = error.get("message").asText();
				}
			}
		} catch (Exception ignored) {
		}
		return new OAuth2LoginException(OAuth2LoginException.PROVIDER_ERROR, message);
	}

	protected static String blankToNull(String value) {
		return value == null || value.isBlank() ? null : value;
	}

	protected static String text(JsonNode node, String field) {
		return node.hasNonNull(field) ? node.get(field).asText() : null;
	}
}
