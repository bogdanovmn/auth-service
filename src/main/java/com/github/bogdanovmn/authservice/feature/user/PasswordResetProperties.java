package com.github.bogdanovmn.authservice.feature.user;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "jwt.reset-password")
public class PasswordResetProperties {
	private long ttlInHours = 1;
}
