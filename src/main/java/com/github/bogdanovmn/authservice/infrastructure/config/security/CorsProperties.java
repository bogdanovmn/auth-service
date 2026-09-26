package com.github.bogdanovmn.authservice.infrastructure.config.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "security.cors")
public class CorsProperties {

	/**
	 * Origins of the frontends allowed to call the API from a browser. An empty
	 * list turns CORS off, which is the right setup when the front is served from
	 * the same origin through a reverse proxy prefix (the production setup).
	 * <p>
	 * Override with {@code SECURITY_CORS_ALLOWED_ORIGINS} (comma separated).
	 */
	private List<String> allowedOrigins = new ArrayList<>();

	/**
	 * The same as {@link #allowedOrigins}, but the values are patterns that may
	 * contain wildcards, so any port of a host can be allowed. The only difference
	 * from {@code "*"} is that credentials stay allowed, which browsers forbid for
	 * the bare wildcard.
	 * <p>
	 * The vite dev server moves to the next free port when {@code 5173} is taken,
	 * so the local dev setup relies on patterns like {@code http://localhost:[*]}
	 * instead of a single port. Override with
	 * {@code SECURITY_CORS_ALLOWED_ORIGIN_PATTERNS} (comma separated).
	 */
	private List<String> allowedOriginPatterns = new ArrayList<>();

	private List<String> allowedMethods = List.of("GET", "POST", "PUT", "DELETE", "OPTIONS");

	private List<String> allowedHeaders = List.of("Authorization", "Content-Type");

	private List<String> exposedHeaders = List.of("Retry-After");

	private boolean allowCredentials = true;

	private long maxAge = 1800;
}
