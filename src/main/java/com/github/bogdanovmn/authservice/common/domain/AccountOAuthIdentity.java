package com.github.bogdanovmn.authservice.common.domain;

import lombok.Builder;
import lombok.ToString;
import lombok.Value;

import java.util.Date;
import java.util.UUID;

@Value
@Builder(toBuilder = true)
@ToString(onlyExplicitlyIncluded = true)
class AccountOAuthIdentity {

	@ToString.Include
	UUID id;

	@ToString.Include
	UUID accountId;

	@ToString.Include
	String provider;

	@ToString.Include
	String providerUserId;

	@ToString.Include
	String email;

	String name;

	Date createdAt;

	Date updatedAt;
}
