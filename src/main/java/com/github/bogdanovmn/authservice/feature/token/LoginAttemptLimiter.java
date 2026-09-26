package com.github.bogdanovmn.authservice.feature.token;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.bogdanovmn.authservice.common.domain.TooManyAttemptsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@EnableConfigurationProperties(LoginAttemptProperties.class)
@RequiredArgsConstructor
@Slf4j
class LoginAttemptLimiter {

	private final LoginAttemptProperties properties;

	private Cache<String, Attempt> attempts;

	@PostConstruct
	public void cacheInit() {
		attempts = Caffeine.newBuilder()
			.expireAfterWrite(
				properties.getWindowInSeconds() + properties.getMaxLockInSeconds() + 1,
				TimeUnit.SECONDS
			)
			.maximumSize(properties.getMaxTrackedEmails())
			.build();
	}

	// Throttling by email is deliberately bounded by short lock windows:
	// otherwise an attacker could lock a known account out for a long time by
	// sending a few wrong passwords on its behalf.
	void ensureNotBlocked(String email) {
		if (!properties.isEnabled()) {
			return;
		}
		long retryAfterSeconds = Optional.ofNullable(attempts.getIfPresent(key(email)))
			.map(attempt -> attempt.retryAfterSeconds(Instant.now()))
			.orElse(0L);

		if (retryAfterSeconds > 0) {
			log.warn(
				"Login is throttled for email: {}, retry after {} sec",
				key(email), retryAfterSeconds
			);
			throw new TooManyAttemptsException(retryAfterSeconds);
		}
	}

	void onFailure(String email) {
		if (!properties.isEnabled()) {
			return;
		}
		String emailKey = key(email);
		Attempt attempt = attempts.asMap().compute(emailKey, (key, previous) -> nextAttempt(previous, Instant.now()));
		if (attempt.lockedUntil() != null) {
			log.warn(
				"Login is throttled for email: {} after {} failed attempts",
				emailKey, attempt.failures()
			);
		}
	}

	void onSuccess(String email) {
		if (!properties.isEnabled()) {
			return;
		}
		attempts.invalidate(key(email));
	}

	private Attempt nextAttempt(Attempt previous, Instant now) {
		long windowInSeconds = properties.getWindowInSeconds();
		if (previous == null || now.isAfter(previous.windowStartedAt().plusSeconds(windowInSeconds))) {
			return new Attempt(1, now, null);
		}
		long maxFailures = properties.getMaxFailures();
		long failures = previous.failures() + 1;
		return new Attempt(
			failures,
			previous.windowStartedAt(),
			failures < maxFailures ? null : now.plusSeconds(lockInSeconds(failures))
		);
	}

	private long lockInSeconds(long failures) {
		long maxFailures = properties.getMaxFailures();
		long shift = Math.min(failures - maxFailures, 16);
		return Math.min(
			properties.getBaseLockInSeconds() << shift,
			properties.getMaxLockInSeconds()
		);
	}

	private String key(String email) {
		return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
	}

	private record Attempt(long failures, Instant windowStartedAt, Instant lockedUntil) {
		long retryAfterSeconds(Instant now) {
			if (lockedUntil == null) {
				return 0;
			}
			long seconds = Duration.between(now, lockedUntil).toSeconds();
			return seconds < 0 ? 0 : Math.max(1, seconds);
		}
	}
}
