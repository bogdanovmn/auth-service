package com.github.bogdanovmn.authservice.feature.user;

import com.github.bogdanovmn.authservice.common.domain.Account;
import lombok.Builder;
import lombok.Value;

import javax.validation.constraints.NotNull;

@Value
@Builder
class UpdateUserStatusRequest {
	@NotNull
	Account.Status status;
}
