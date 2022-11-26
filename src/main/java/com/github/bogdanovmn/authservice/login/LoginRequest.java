package com.github.bogdanovmn.authservice.login;

import lombok.Builder;
import lombok.Value;

import javax.validation.constraints.NotEmpty;

@Value
@Builder
class LoginRequest {
	@NotEmpty
	String email;
	@NotEmpty
	String password;
}
