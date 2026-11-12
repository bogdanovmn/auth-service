package com.github.bogdanovmn.authservice.feature.activity;

import lombok.Builder;
import lombok.Value;

import java.util.Date;
import java.util.UUID;

@Value
@Builder
class SecurityEventResponse {
	Long id;
	String type;
	String ip;
	String userAgent;
	String clientUrl;
	String provider;
	Date createdAt;
	UUID accountId;
	String accountName;
	String accountEmail;
}
