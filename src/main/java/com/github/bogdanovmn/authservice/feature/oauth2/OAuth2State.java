package com.github.bogdanovmn.authservice.feature.oauth2;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
class OAuth2State {
	String provider;
	String from;
	String clientUrl;
	String codeVerifier;
	Instant createdAt;
}
