package com.github.bogdanovmn.authservice.feature.token;

import com.github.bogdanovmn.authservice.common.domain.TooManyAttemptsException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(
	classes = LoginAttemptLimiter.class,
	properties = {
		"security.login-attempt.max-failures=3",
		"security.login-attempt.window-in-seconds=60",
		"security.login-attempt.base-lock-in-seconds=1",
		"security.login-attempt.max-lock-in-seconds=60"
	}
)
@ExtendWith(SpringExtension.class)
class LoginAttemptLimiterTest {

	@Autowired
	private LoginAttemptLimiter limiter;

	@Test
	void emailIsBlockedAfterMaxFailures() {
		limiter.onFailure("blocked@mail.ru");
		limiter.onFailure("blocked@mail.ru");
		assertDoesNotThrow(() -> limiter.ensureNotBlocked("blocked@mail.ru"));

		limiter.onFailure("blocked@mail.ru");

		TooManyAttemptsException ex = assertThrows(
			TooManyAttemptsException.class,
			() -> limiter.ensureNotBlocked("blocked@mail.ru")
		);
		assertEquals(1L, ex.getRetryAfterSeconds());
	}

	@Test
	void successfulLoginResetsCounter() {
		limiter.onFailure("reset@mail.ru");
		limiter.onFailure("reset@mail.ru");
		limiter.onSuccess("reset@mail.ru");

		limiter.onFailure("reset@mail.ru");
		limiter.onFailure("reset@mail.ru");

		assertDoesNotThrow(() -> limiter.ensureNotBlocked("reset@mail.ru"));
	}

	@Test
	void lockIsReleasedAfterExpiration() throws InterruptedException {
		limiter.onFailure("expiring@mail.ru");
		limiter.onFailure("expiring@mail.ru");
		limiter.onFailure("expiring@mail.ru");

		assertThrows(
			TooManyAttemptsException.class,
			() -> limiter.ensureNotBlocked("expiring@mail.ru")
		);

		Thread.sleep(1500);

		assertDoesNotThrow(() -> limiter.ensureNotBlocked("expiring@mail.ru"));
	}

	@Test
	void emailIsNormalized() {
		limiter.onFailure("normalized@mail.ru");
		limiter.onFailure("normalized@mail.ru");
		limiter.onFailure("NORMALIZED@Mail.RU");

		assertThrows(
			TooManyAttemptsException.class,
			() -> limiter.ensureNotBlocked(" normalized@MAIL.ru ")
		);
	}

	@Test
	void unknownEmailIsNotBlocked() {
		assertDoesNotThrow(() -> limiter.ensureNotBlocked("nobody@mail.ru"));
	}
}
