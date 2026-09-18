package com.github.bogdanovmn.authservice.feature.activity;

import lombok.Builder;
import lombok.Value;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Value
@Builder
class UserActivityResponse {
	AccountInfo account;
	List<Event> events;

	@Value
	@Builder
	static class AccountInfo {
		UUID id;
		String name;
		String email;
	}

	@Value
	@Builder
	static class Event {
		Long id;
		String type;
		String ip;
		String userAgent;
		Date createdAt;
	}
}