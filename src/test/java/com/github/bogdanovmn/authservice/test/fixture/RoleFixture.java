package com.github.bogdanovmn.authservice.test.fixture;

import com.github.bogdanovmn.authservice.common.domain.Application;
import com.github.bogdanovmn.authservice.common.domain.Role;
import lombok.Builder;
import lombok.Value;


@Value
@Builder
public class RoleFixture {
	String appName;
	Role.Name roleName;

	public static Role standardUser() {
		return RoleFixture.builder()
			.appName(Application.ANY_APPLICATION)
			.roleName(Role.Name.user)
			.build()
			.role();
	}

	public static Role moderator(String appName) {
		return RoleFixture.builder()
			.appName(appName)
			.roleName(Role.Name.moderator)
			.build()
			.role();
	}

	public Role role() {
		return Role.builder()
			.name(roleName)
			.application(
				Application.builder()
					.name(appName)
					.build()
			)
			.build();
	}
}
