package com.github.bogdanovmn.authservice.common.domain;

public class AccountNotAvailableException extends RuntimeException {
	public AccountNotAvailableException(Account account) {
		super(
			"The account '%s' is not available: its status is %s".formatted(
				account.getEmail(), account.getStatus()
			)
		);
	}
}
