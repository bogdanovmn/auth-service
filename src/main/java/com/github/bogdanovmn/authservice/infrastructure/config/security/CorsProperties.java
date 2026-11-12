package com.github.bogdanovmn.authservice.infrastructure.config.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "security.cors")
public class CorsProperties {

	private List<String> allowedOrigins;

	private List<String> allowedOriginPatterns;

	private List<String> allowedMethods;

	private List<String> allowedHeaders;

	private List<String> exposedHeaders;

	private boolean allowCredentials;

	private long maxAge;
}
