package com.github.bogdanovmn.authservice.login;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
class LoginResponse {
	String token;
	String refreshToken;
}
