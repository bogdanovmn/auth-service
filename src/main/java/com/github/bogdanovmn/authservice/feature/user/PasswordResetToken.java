package com.github.bogdanovmn.authservice.feature.user;

import lombok.Builder;
import lombok.ToString;
import lombok.Value;

import java.util.Date;
import java.util.UUID;

@Value
@Builder
@ToString(onlyExplicitlyIncluded = true)
class PasswordResetToken {

	@ToString.Include
	UUID id;

	@ToString.Include
	UUID accountId;

	Date createdAt;

	@ToString.Include
	Date expiresAt;
}
