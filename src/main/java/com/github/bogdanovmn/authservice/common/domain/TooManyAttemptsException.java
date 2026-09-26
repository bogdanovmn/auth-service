package com.github.bogdanovmn.authservice.common.domain;

import lombok.Getter;

@Getter
public class TooManyAttemptsException extends RuntimeException {
	private static final long SECONDS_IN_MINUTE = 60;

	private final long retryAfterSeconds;

	public TooManyAttemptsException(long retryAfterSeconds) {
		super("Too many failed attempts. Try again in %s.".formatted(waitTime(retryAfterSeconds)));
		this.retryAfterSeconds = retryAfterSeconds;
	}

	private static String waitTime(long seconds) {
		if (seconds < SECONDS_IN_MINUTE) {
			return "%d seconds".formatted(Math.max(seconds, 1));
		}
		return "%d minutes".formatted((seconds + SECONDS_IN_MINUTE - 1) / SECONDS_IN_MINUTE);
	}
}
