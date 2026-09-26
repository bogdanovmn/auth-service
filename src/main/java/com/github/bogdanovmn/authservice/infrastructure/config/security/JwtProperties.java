package com.github.bogdanovmn.authservice.infrastructure.config.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
	private String privateKeyPath = "";
	private String publicKeyPath = "";
	private long ttlInMinutes = 30;
	private final RefreshToken refreshToken = new RefreshToken();

	@Getter
	@Setter
	public static class RefreshToken {
		private long ttlInHours = 720;
	}
}
