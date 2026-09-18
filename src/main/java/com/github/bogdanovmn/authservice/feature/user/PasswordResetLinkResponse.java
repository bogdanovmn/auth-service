package com.github.bogdanovmn.authservice.feature.user;

import lombok.Builder;
import lombok.Value;

import java.util.Date;

@Value
@Builder
class PasswordResetLinkResponse {
	String token;
	Date expiresAt;
	long ttlInMinutes;
}