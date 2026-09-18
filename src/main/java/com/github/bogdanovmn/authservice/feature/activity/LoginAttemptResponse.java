package com.github.bogdanovmn.authservice.feature.activity;

import lombok.Builder;
import lombok.Value;

import java.util.Date;

@Value
@Builder
class LoginAttemptResponse {
	Long id;
	String email;
	String ip;
	String userAgent;
	Date createdAt;
}