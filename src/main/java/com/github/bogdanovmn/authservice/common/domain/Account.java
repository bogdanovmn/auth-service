package com.github.bogdanovmn.authservice.common.domain;

import lombok.Builder;
import lombok.ToString;
import lombok.Value;

import java.util.Date;
import java.util.Set;
import java.util.UUID;

@Value
@Builder(toBuilder = true)
@ToString(onlyExplicitlyIncluded = true)
public class Account {

	public enum Status {
		CREATED, ACTIVE, INACTIVE;

		public boolean isAvailable() {
			return this != INACTIVE;
		}
	}

	@ToString.Include
	UUID id;

	@ToString.Include
	String email;

	String name;

	String encodedPassword;

	@ToString.Include
	@Builder.Default
	Status status = Status.CREATED;

	@Builder.Default
	Set<Role> roles = Set.of();

	Date createdAt;

	Date updatedAt;

	@ToString.Include
	Date passwordChangedAt;
}
