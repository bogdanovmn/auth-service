package com.github.bogdanovmn.authservice.common.domain;

import lombok.Builder;
import lombok.ToString;
import lombok.Value;

import java.util.Date;
import java.util.UUID;

@Value
@Builder
@ToString(onlyExplicitlyIncluded = true)
public class AccountSecurityEvent {

	@ToString.Include
	Long id;

	@ToString.Include
	UUID accountId;

	@ToString.Include
	AccountSecurityEventType type;

	@ToString.Include
	String ip;

	@ToString.Include
	String userAgent;

	@ToString.Include
	String clientUrl;

	@ToString.Include
	String provider;

	Date createdAt;
}
