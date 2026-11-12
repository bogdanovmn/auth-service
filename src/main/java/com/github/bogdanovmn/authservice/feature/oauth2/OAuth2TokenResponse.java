package com.github.bogdanovmn.authservice.feature.oauth2;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
class OAuth2TokenResponse {
	String accessToken;
	String userId;
	String email;
}
