package com.github.bogdanovmn.authservice.common.domain;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
public class AccountSecurityEventWithAccount {
	AccountSecurityEvent event;
	UUID accountId;
	String accountName;
	String accountEmail;
}
