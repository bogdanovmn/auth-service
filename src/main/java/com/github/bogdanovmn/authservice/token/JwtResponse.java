package com.github.bogdanovmn.authservice.token;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
class JwtResponse {
	String token;
	String refreshToken;
}
