package com.github.bogdanovmn.authservice.feature.management;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.List;

@Value
@Builder
class ApplicationStatistic {
	Long id;
	String name;
	@Singular
	List<Role> roles;

	record Role(String name, int usersCount) {
	}
}
