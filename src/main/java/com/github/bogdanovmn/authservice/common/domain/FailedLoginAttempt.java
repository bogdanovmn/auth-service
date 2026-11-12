package com.github.bogdanovmn.authservice.common.domain;

import lombok.Builder;
import lombok.ToString;
import lombok.Value;

import java.util.Date;

@Value
@Builder
@ToString(onlyExplicitlyIncluded = true)
public class FailedLoginAttempt {

	@ToString.Include
	Long id;

	@ToString.Include
	String email;

	@ToString.Include
	String ip;

	@ToString.Include
	String userAgent;

	Date createdAt;
}
