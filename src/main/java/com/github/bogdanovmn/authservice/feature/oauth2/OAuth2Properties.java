package com.github.bogdanovmn.authservice.feature.oauth2;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@Getter
@Setter
@ConfigurationProperties(prefix = "auth.oauth2")
class OAuth2Properties {

	private String redirectBaseUrl;
	private String frontendUrl;
	private long stateTtlInSeconds;
	private Map<String, Provider> providers;

	@Getter
	@Setter
	static class Provider {
		private String clientId;
		private String clientSecret;
		private String scope;
		private String mode;
		private String apiVersion;
		private String authorizeUrl;
		private String tokenUrl;
		private String userInfoUrl;
	}
}
