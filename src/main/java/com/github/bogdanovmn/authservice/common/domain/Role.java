package com.github.bogdanovmn.authservice.common.domain;

import lombok.Builder;
import lombok.Value;

import java.util.Date;

@Value
@Builder
public class Role {
	public enum Name { user, admin, moderator }

	Long id;
	Name name;
	Application application;
	Date createdAt;

	@Override
	public String toString() {
		return "%s:%s".formatted(
			getApplication().getName(),
			getName()
		);
	}
}
