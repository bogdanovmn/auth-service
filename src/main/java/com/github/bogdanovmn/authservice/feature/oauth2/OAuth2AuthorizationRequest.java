package com.github.bogdanovmn.authservice.feature.oauth2;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
class OAuth2AuthorizationRequest {
	String provider;
	OAuth2Properties.Provider config;
	String redirectUri;
	String state;
	String codeChallenge;
}
