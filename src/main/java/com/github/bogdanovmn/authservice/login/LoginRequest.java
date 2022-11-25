package com.github.bogdanovmn.authservice.login;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
class LoginRequest {
	String email;
	String password;
}
