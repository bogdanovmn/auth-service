package com.github.bogdanovmn.authservice.registration;

import lombok.Builder;
import lombok.Value;

import javax.validation.constraints.NotEmpty;

@Value
@Builder
class RegistrationRequest {
	@NotEmpty
	String accountName;
	@NotEmpty
	String password;
	@NotEmpty
	String email;
}
