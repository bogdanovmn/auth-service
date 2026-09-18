package com.github.bogdanovmn.authservice.feature.user;

import lombok.Builder;
import lombok.Value;

import javax.validation.constraints.NotEmpty;

@Value
@Builder
class ResetPasswordRequest {
	@NotEmpty
	String token;
	@NotEmpty
	String password;
}