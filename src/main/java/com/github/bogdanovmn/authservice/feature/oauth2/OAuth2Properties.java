package com.github.bogdanovmn.authservice.feature.oauth2;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
@ConfigurationProperties(prefix = "auth.oauth2")
public class OAuth2Properties {

	private String redirectBaseUrl = "http://localhost:8091";

	private String frontendUrl = "http://localhost:5173/sso/";

	private long stateTtlInSeconds = 600;

	private Map<String, Provider> providers = new LinkedHashMap<>();

	@Getter
	@Setter
	public static class Provider {
		private String clientId = "";
		private String clientSecret = "";
		private String scope = "";
		private String mode = "id";
		private String apiVersion = "5.199";
		private String authorizeUrl;
		private String tokenUrl;
		private String userInfoUrl;
	}
}
