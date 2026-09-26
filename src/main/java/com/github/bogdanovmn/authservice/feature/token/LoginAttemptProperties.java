package com.github.bogdanovmn.authservice.feature.token;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "security.login-attempt")
class LoginAttemptProperties {
	private boolean enabled = true;
	private long maxFailures = 5;
	private long windowInSeconds = 900;
	private long baseLockInSeconds = 60;
	private long maxLockInSeconds = 3600;
	private long maxTrackedEmails = 100_000;
}
