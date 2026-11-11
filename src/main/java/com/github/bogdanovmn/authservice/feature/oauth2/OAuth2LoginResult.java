package com.github.bogdanovmn.authservice.feature.oauth2;

import com.github.bogdanovmn.authservice.common.domain.Account;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class OAuth2LoginResult {
	Account account;
	String from;
}
