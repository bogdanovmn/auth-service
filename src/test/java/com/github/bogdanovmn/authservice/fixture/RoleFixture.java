package com.github.bogdanovmn.authservice.fixture;

import com.github.bogdanovmn.authservice.model.Application;
import com.github.bogdanovmn.authservice.model.Role;


public record RoleFixture(String appName, Role.Name roleName) {

	public static Role standardUser() {
		return new RoleFixture(Application.ANY_APPLICATION, Role.Name.user).role();
	}

	public static Role moderator(String appName) {
		return new RoleFixture(appName, Role.Name.moderator).role();
	}

	public Role role() {
		return new Role()
			.setName(roleName)
			.setApplication(
				new Application()
					.setName(appName)
			);
	}
}
