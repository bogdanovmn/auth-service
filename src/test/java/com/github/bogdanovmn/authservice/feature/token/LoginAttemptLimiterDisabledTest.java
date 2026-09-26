package com.github.bogdanovmn.authservice.feature.token;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest(
	classes = LoginAttemptLimiter.class,
	properties = {
		"security.login-attempt.enabled=false",
		"security.login-attempt.max-failures=1"
	}
)
@ExtendWith(SpringExtension.class)
class LoginAttemptLimiterDisabledTest {

	@Autowired
	private LoginAttemptLimiter limiter;

	@Test
	void failuresAreNotTracked() {
		limiter.onFailure("any@mail.ru");
		limiter.onFailure("any@mail.ru");
		limiter.onFailure("any@mail.ru");

		assertDoesNotThrow(() -> limiter.ensureNotBlocked("any@mail.ru"));
	}
}
