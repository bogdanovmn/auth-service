package com.github.bogdanovmn.authservice.feature.token;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "security.login-attempt")
class LoginAttemptProperties {
	private boolean enabled;
	private long maxFailures;
	private long windowInSeconds;
	private long baseLockInSeconds;
	private long maxLockInSeconds;
	private long maxTrackedEmails;
}
