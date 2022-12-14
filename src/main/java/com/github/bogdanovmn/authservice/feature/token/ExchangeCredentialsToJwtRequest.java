package com.github.bogdanovmn.authservice.feature.token;

import lombok.Builder;
import lombok.Value;

import javax.validation.constraints.NotEmpty;

@Value
@Builder
class ExchangeCredentialsToJwtRequest {
	@NotEmpty
	String email;
	@NotEmpty
	String password;
}
