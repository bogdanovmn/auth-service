package com.github.bogdanovmn.authservice.feature.token;

import lombok.Builder;
import lombok.Value;

import javax.validation.constraints.NotEmpty;

@Value
@Builder
class RefreshJwtRequest {
	@NotEmpty
	String refreshToken;
}
